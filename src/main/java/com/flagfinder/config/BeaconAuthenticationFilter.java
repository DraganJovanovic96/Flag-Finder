package com.flagfinder.config;

import com.flagfinder.service.impl.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication filter specifically for beacon requests (room cancellation).
 * Handles JWT authentication for the room cancel endpoint with flexible token extraction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BeaconAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Service for JWT token operations.
     */
    private final JwtService jwtService;
    
    /**
     * Service for loading user details during authentication.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Processes authentication for beacon requests.
     * Only applies to POST requests to /api/v1/rooms/cancel endpoint.
     * Supports token extraction from both query parameters and Authorization header.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/v1/rooms/cancel") || 
            !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token;
        final String userEmail;

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            token = tokenParam;
        } else {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            token = authHeader.substring(7);
        }

        try {
            userEmail = jwtService.extractUsername(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
