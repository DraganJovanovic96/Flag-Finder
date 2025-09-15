package com.flagfinder.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * Service interface for game name operations.
 * Provides methods for setting and validating user game names.
 */
public interface GameNameService {
    /**
     * Sets the game name for the authenticated user.
     *
     * @param request the request containing the new game name
     * @param authentication the authentication context of the user
     * @return response entity with success or error information
     */
    ResponseEntity<?> setGameName(Map<String, String> request, Authentication authentication);
    
    /**
     * Checks if a game name is available for use.
     *
     * @param gameName the game name to check for availability
     * @return response entity indicating availability status
     */
    ResponseEntity<?> checkGameNameAvailability(String gameName);
}
