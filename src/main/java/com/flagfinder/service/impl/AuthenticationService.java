package com.flagfinder.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagfinder.dto.*;
import com.flagfinder.enumeration.TokenType;
import com.flagfinder.model.Token;
import com.flagfinder.model.User;
import com.flagfinder.repository.TokenRepository;
import com.flagfinder.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service class for handling authentication-related operations.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    /**
     * The repository used to retrieve user data.
     */
    private final UserRepository repository;

    /**
     * The repository used to retrieve token data.
     */
    private final TokenRepository tokenRepository;

    /**
     * Service interface for encoding passwords. The preferred implementation is BCryptPasswordEncoder.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * The Service used to retrieve token data.
     */
    private final com.flagfinder.service.impl.JwtService jwtService;

    /**
     * Processes an Authentication request.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * The Service used to send email.
     */
    private final com.flagfinder.service.impl.EmailServiceImpl emailService;

    /**
     * The characters used to create password code.
     */
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * The length of password code.
     */
    private static final int CODE_LENGTH = 64;

    @Value("${spring.frontend.url}")
    private String frontendUrl;


    /**
     * Registers a new user.
     *
     * @param request the registration request data
     * @return the authentication response containing the access token and refresh token
     */
    public AuthenticationResponseDto register(@Valid RegisterRequestDto request) throws UnsupportedEncodingException {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        String token = generateVerificationCode();
        user.setVerificationCode(passwordEncoder.encode(token));
        user.setVerificationExpiration(LocalDateTime.now().plusHours(3));
        user.setEnabled(false);
        sendVerificationEmail(token, user.getEmail());
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void setPasswordResetCode(EmailRequestDto emailRequestDto) throws UnsupportedEncodingException {
        User user = repository.findByEmail(emailRequestDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, " User doesn't exist"));

        String generatedToken = generateVerificationCode();

        user.setPasswordCode(passwordEncoder.encode(generatedToken));
        user.setPasswordCodeExpiration(LocalDateTime.now().plusMinutes(60));
        revokeAllUserTokens(user);
        repository.save(user);
        sendResetPasswordLink(emailRequestDto.getEmail(), generatedToken);
    }

    public AuthenticationResponseDto resetPassword(String passwordCode, String email, PasswordResetDto passwordResetDto) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, " User doesn't exist"));

        if (!passwordEncoder.matches(passwordCode, user.getPasswordCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset code");
        }

        if (user.getPasswordCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, " Password link has expired");
        }

        if (!passwordResetDto.getNewPassword().equals(passwordResetDto.getRepeatNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords don't match");
        }

        user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
        user.setPasswordCode(null);
        user.setPasswordCodeExpiration(null);
        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Authenticates a user.
     *
     * @param request the authentication request data
     * @return the authentication response containing the access token and refresh token
     */
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, " Account is not verified");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Saves a user token.
     *
     * @param user     the user associated with the token
     * @param jwtToken the JWT token to be saved
     */
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    /**
     * Revokes all tokens associated with a user.
     *
     * @param user the user whose tokens should be revoked
     */
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    /**
     * Refreshes the access token.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @throws IOException if an I/O error occurs
     */
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String refreshHeader = request.getHeader("Refresh");
        final String refreshToken;
        final String userEmail;
        if (refreshHeader == null || !refreshHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = refreshHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail).orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public AuthenticationResponseDto verifyUser(String token, String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, " User doesn't exist"));

        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, " User is already verified");
        }

        if (!passwordEncoder.matches(token, user.getVerificationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset code");
        }

        if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, " Verification code has expired");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void resendVerificationCode(String email) throws UnsupportedEncodingException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, " User doesn't exist"));

        if (user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, " User is already verified");
        }

        String token = generateVerificationCode();
        user.setVerificationCode(passwordEncoder.encode(token));
        user.setVerificationExpiration(LocalDateTime.now().plusHours(3));
        sendVerificationEmail(token, user.getEmail());
        repository.save(user);
    }

    public void sendVerificationEmail(String token, String email) throws UnsupportedEncodingException {
        String subject = "MSS Account Verification";

        String verificationLink = frontendUrl + "verify?token=" +
                URLEncoder.encode(token, "UTF-8") +
                "&email=" + URLEncoder.encode(email, "UTF-8");

        String htmlMessage = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>User Verification</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      margin: 0;
                      padding: 0;
                      background-color: #f4f4f9;
                    }
                    .email-container {
                      max-width: 600px;
                      margin: 20px auto;
                      background: #ffffff;
                      border: 1px solid #dddddd;
                      border-radius: 8px;
                      overflow: hidden;
                      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    .email-header {
                      background-color: #2c2b29;
                      padding: 20px;
                      text-align: center;
                    }
                    .email-header img {
                      max-width: 150px;
                    }
                    .email-body {
                      padding: 20px;
                    }
                    .email-body h1 {
                      color: #333333;
                      font-size: 24px;
                    }
                    .email-body p {
                      color: #555555;
                      font-size: 16px;
                      line-height: 1.5;
                    }
                    .verification-code {
                      display: flex;
                      justify-content: center;
                      align-items: center;
                      margin: 20px 0;
                      padding: 20px;
                      background-color: #e9ecef;
                      border: 1px dashed #6c757d;
                      font-size: 18px;
                      font-weight: bold;
                      color: #004085;
                      text-align: center;
                    }
                    .email-footer {
                      padding: 20px;
                      text-align: center;
                      background-color: #f8f9fa;
                      color: #6c757d;
                      font-size: 14px;
                    }
                    .email-footer a {
                      color: #007bff;
                      text-decoration: none;
                    }
                    .email-footer a:hover {
                      text-decoration: underline;
                    }
                  </style>
                </head>
                <body>
                  <div class="email-container">
                    <div class="email-header">
                      <img src="https://i.imghippo.com/files/pQi9349bTk.png" alt="Logo">
                    </div>
                    <div class="email-body">
                      <h1>Verify Your Email Address</h1>
                      <p>Thank you for signing up! Please click the verification button below to complete your registration:</p>
                      <div>
                        <a href="%s" style="display: block; text-align: center; background-color: #007bff; color: #ffffff; padding: 12px 20px; text-decoration: none; font-size: 18px; border-radius: 5px;">Verify User</a>
                      </div>
                      <p>If you believe you got this email by mistake, please ignore this email or contact support if you have any questions.</p>
                    </div>
                    <div class="email-footer">
                      <p>Need help? Contact us at <a href="mailto:support@mss.com">support@mss.com</a></p>
                      <p>&copy; 2024 MSS. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(verificationLink);

        try {
            emailService.sendVerificationEmail(email, subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendResetPasswordLink(String email, String generatedToken) throws UnsupportedEncodingException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist"));

        String resetLink = frontendUrl + "reset-password?token=" +
                URLEncoder.encode(generatedToken, "UTF-8") +
                "&email=" + URLEncoder.encode(email, "UTF-8");

        String subject = "MSS Password Reset";

        String htmlMessage = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Password Reset</title>
                </head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 20px;">
                  <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; border: 1px solid #dddddd; overflow: hidden;">
                    <div style="background-color: #2c2b29; text-align: center; padding: 20px;">
                      <img src="https://i.imghippo.com/files/pQi9349bTk.png" alt="Logo" style="max-width: 150px;">
                    </div>
                    <div style="padding: 20px;">
                      <h1 style="color: #333333; font-size: 24px;">Password Reset</h1>
                      <p style="color: #555555; font-size: 16px; line-height: 1.5;">Oops, it seems you have forgotten your password for <strong>%s</strong>. No worries, you can reset it by clicking:</p>
                      <a href="%s" style="display: block; text-align: center; background-color: #007bff; color: #ffffff; padding: 12px 20px; text-decoration: none; font-size: 18px; border-radius: 5px;">Reset Password</a>
                      <p style="color: #555555; font-size: 14px; line-height: 1.5;">If you did not request a change of password, please ignore this email.</p>
                    </div>
                    <div style="text-align: center; background-color: #f8f9fa; padding: 10px; color: #6c757d; font-size: 14px;">
                      <p>Need help? Contact us at <a href="mailto:support@mss.com" style="color: #007bff; text-decoration: none;">support@mss.com</a></p>
                      <p>&copy; 2024 MSS. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(email, resetLink);

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
        }
    }

    public static String generateVerificationCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder verificationCode = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            verificationCode.append(CHARACTERS.charAt(randomIndex));
        }

        return verificationCode.toString();
    }
}
