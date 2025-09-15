package com.flagfinder.service;

import com.flagfinder.dto.AuthenticationResponseDto;
import org.springframework.security.oauth2.core.user.OAuth2User;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Service interface for OAuth2 authentication operations.
 * Provides methods for processing OAuth2 users and handling authentication success scenarios.
 */
public interface OAuth2Service {
    /**
     * Processes an OAuth2 user and creates authentication response.
     *
     * @param oauth2User the OAuth2 user information from the provider
     * @return authentication response DTO with tokens
     * @throws RuntimeException if user processing fails
     */
    AuthenticationResponseDto processOAuth2User(OAuth2User oauth2User);
    
    /**
     * Handles OAuth2 authentication success and redirects user.
     *
     * @param userParams the user parameters from OAuth2 flow
     * @param response the HTTP servlet response for redirection
     * @throws IOException if response writing fails
     */
    void handleOAuth2Success(Map<String, String> userParams, HttpServletResponse response) throws IOException;
}
