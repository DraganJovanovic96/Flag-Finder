package com.flagfinder.service.impl;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.model.Token;
import com.flagfinder.model.User;
import com.flagfinder.repository.TokenRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.impl.JwtService;
import com.flagfinder.service.OAuth2Service;
import com.flagfinder.enumeration.Role;
import com.flagfinder.enumeration.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of OAuth2Service interface.
 * Handles OAuth2 authentication processing for Google OAuth2 integration.
 * Manages user creation, updates, and JWT token generation for OAuth2 users.
 * Provides seamless integration between Google OAuth2 and the application's authentication system.
 */
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    
    @Value("${spring.frontend.url}")
    private String frontendUrl;

    /**
     * Processes an OAuth2 user and creates or updates the corresponding application user.
     * Extracts user information from OAuth2 attributes, handles user creation or updates,
     * and generates JWT tokens for authentication.
     *
     * @param oauth2User the OAuth2 user object containing user attributes from Google
     * @return AuthenticationResponseDto containing access and refresh tokens
     */
    @Override
    public AuthenticationResponseDto processOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String pictureUrl = oauth2User.getAttribute("picture");
        
        
        if (firstName == null && name != null && name.contains(" ")) {
            String[] nameParts = name.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else if (firstName == null && name != null) {
            firstName = name;
        }


        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByEmail(email);
        }
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            boolean needsUpdate = false;
            
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                needsUpdate = true;
            }
            if (firstName != null && !firstName.equals(user.getFirstname())) {
                user.setFirstname(firstName);
                needsUpdate = true;
            }
            
            if (lastName != null && !lastName.equals(user.getLastname())) {
                user.setLastname(lastName);
                needsUpdate = true;
            }
            
            if (pictureUrl != null && !pictureUrl.equals(user.getImageUrl())) {
                user.setImageUrl(pictureUrl);
                needsUpdate = true;
            }
            
            
            if (needsUpdate) {
                userRepository.save(user);
            }
        } else {
            String uniqueGameName = generateUniqueGameName(name);
            
            user = User.builder()
                    .email(email)
                    .gameName(uniqueGameName)
                    .firstname(firstName)
                    .lastname(lastName)
                    .imageUrl(pictureUrl)
                    .googleId(googleId)
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("gameName", user.getGameName());
        
        String jwtToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Revokes all existing valid tokens for a user.
     * Marks all user's tokens as expired and revoked to ensure security.
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
     * Saves a new JWT token for a user in the token repository.
     * Creates a new token entity with the provided JWT token.
     *
     * @param user the user to associate the token with
     * @param jwtToken the JWT token string to save
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
     * Generates a unique game name based on the user's display name.
     * Sanitizes the name, ensures uniqueness by appending numbers if needed,
     * and enforces length constraints.
     *
     * @param name the base name to generate a game name from
     * @return a unique game name that doesn't conflict with existing users
     */
    private String generateUniqueGameName(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        
        String baseName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (baseName.length() > 15) {
            baseName = baseName.substring(0, 15);
        }
        
        String gameName = baseName;
        int counter = 1;
        
        while (userRepository.findOneByGameNameIgnoreCase(gameName).isPresent()) {
            gameName = baseName + counter;
            counter++;
        }
        
        return gameName;
    }

    /**
     * Handles successful OAuth2 authentication by processing user parameters
     * and redirecting to appropriate frontend URLs with authentication tokens.
     * Determines if user needs initial setup and redirects accordingly.
     *
     * @param userParams map containing user information from OAuth2 provider
     * @param response HTTP response object for sending redirects
     * @throws IOException if redirect operation fails
     */
    @Override
    public void handleOAuth2Success(Map<String, String> userParams, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("email", userParams.get("email"));
            attributes.put("name", userParams.get("name"));
            attributes.put("sub", userParams.get("googleId"));
            
            if (userParams.get("picture") != null) attributes.put("picture", userParams.get("picture"));
            if (userParams.get("givenName") != null) attributes.put("given_name", userParams.get("givenName"));
            if (userParams.get("familyName") != null) attributes.put("family_name", userParams.get("familyName"));
            if (userParams.get("locale") != null) attributes.put("locale", userParams.get("locale"));
            
            OAuth2User oauth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
            );
            
            AuthenticationResponseDto authResponse = processOAuth2User(oauth2User);
            
            User user = userRepository.findByEmail(userParams.get("email")).orElse(null);
            boolean isNewUser = user != null && !user.isInitialSetupCompleted();
            
            if (isNewUser) {
                String redirectUrl = String.format("%s/setup-gamename?token=%s&refreshToken=%s&currentGameName=%s", 
                    frontendUrl, authResponse.getAccessToken(), authResponse.getRefreshToken(), 
                    java.net.URLEncoder.encode(user.getGameName(), "UTF-8"));
                response.sendRedirect(redirectUrl);
            } else {
                String redirectUrl = String.format("%s/oauth2/callback?token=%s&refreshToken=%s", 
                    frontendUrl, authResponse.getAccessToken(), authResponse.getRefreshToken());
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            String errorUrl = String.format("%s/login?error=oauth2_failed", frontendUrl);
            response.sendRedirect(errorUrl);
        }
    }
}
