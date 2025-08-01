package com.flagfinder.controller;


import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.service.impl.AuthenticationService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Controller class for handling authentication-related API endpoints.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    /**
     * The service used to for authentication.
     */
    private final AuthenticationService service;

    /**
     * Authenticates a user.
     *
     * @param request the authentication request containing user credentials
     * @return the ResponseEntity containing the authentication response
     */
    @PostMapping("/authenticate")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully logged in.", response = AuthenticationResponseDto.class),
            @ApiResponse(code = 403, message = "Account is not verified.")
    })
    public ResponseEntity<com.flagfinder.dto.AuthenticationResponseDto> authenticate(
            @RequestBody com.flagfinder.dto.AuthenticationRequestDto request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    /**
     * Refreshes the authentication token.
     *
     * @param request  the HttpServletRequest containing the refresh token
     * @param response the HttpServletResponse for setting the new token in the response header
     * @throws IOException if an I/O error occurs while refreshing the token
     */
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    /**
     * Verifies a user's account using a verification code.
     *
     * @param token verification code of the user.
     * @return a {@link ResponseEntity} containing an {@link com.flagfinder.dto.AuthenticationResponseDto} with the verification status
     * @throws ResponseStatusException if the verification code is invalid, expired, or if the user is already verified
     */
    @PostMapping("/verification")
    public ResponseEntity<com.flagfinder.dto.AuthenticationResponseDto> verifyUser(@RequestParam String token,
                                                                            @RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.verifyUser(token, email));
    }

    /**
     * Resends the verification code to the user's email.
     *
     * @param emailRequestDto the email address of the user to whom the verification code should be sent
     * @return a {@link ResponseEntity} containing a success message if the code was sent successfully,
     * or an error message if the operation fails
     * @throws RuntimeException if an error occurs while resending the verification code
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> reVerifyUser(@RequestBody com.flagfinder.dto.EmailRequestDto emailRequestDto) {
        try {
            service.resendVerificationCode(emailRequestDto.getEmail());
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Verification code sent");
        } catch (RuntimeException | UnsupportedEncodingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Resends the verification code to the user's email.
     *
     * @param emailRequestDto the email address of the user to whom the verification code should be sent
     * @return a {@link ResponseEntity} containing a success message if the code was sent successfully,
     * or an error message if the operation fails
     * @throws RuntimeException if an error occurs while resending the verification code
     */
    @PostMapping("/send-reset-password")
    public ResponseEntity<?> sendPasswordResetCode(@RequestBody com.flagfinder.dto.EmailRequestDto emailRequestDto) {
        try {
            service.setPasswordResetCode(emailRequestDto);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Password reset code sent");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Handles the password reset request.
     * <p>
     * This endpoint is used when a user has forgotten their password. The user provides
     * the reset token via the query parameter and submits the new password along with its confirmation.
     *
     * @param token            The password reset token sent to the user's email.
     * @param passwordResetDto Data Transfer Object containing the new password and confirmation.
     * @return A response entity containing the authentication response if the password reset is successful.
     * @throws ResponseStatusException if the token is invalid or the password reset fails.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<com.flagfinder.dto.AuthenticationResponseDto> resetPassword(
            @RequestParam String token,
            @RequestParam String email,
            @RequestBody com.flagfinder.dto.PasswordResetDto passwordResetDto) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(service.resetPassword(token, email, passwordResetDto));
    }
}
