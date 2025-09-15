package com.flagfinder.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for extracting authenticated user information from Spring Security context.
 * Provides utility methods to retrieve the currently authenticated user's details
 * from the security context in a safe and consistent manner.
 */
@Service
@RequiredArgsConstructor
public class ExtractAuthenticatedUserService {

    /**
     * Retrieves the username (email) of the currently authenticated user.
     * Extracts the authentication information from the Spring Security context
     * and returns the username from the UserDetails principal.
     *
     * @return the username (email) of the authenticated user
     * @throws ResponseStatusException with UNAUTHORIZED status if user is not authenticated
     * @throws ResponseStatusException with BAD_REQUEST status if authentication principal is invalid
     */
    public String getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authentication principal is invalid");
    }
}
