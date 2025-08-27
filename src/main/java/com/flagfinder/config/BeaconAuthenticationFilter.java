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

@Component
@RequiredArgsConstructor
@Slf4j
public class BeaconAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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

        log.info("BeaconAuthenticationFilter processing request: {} {}", request.getMethod(), request.getRequestURI());

        final String token;
        final String userEmail;

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            token = tokenParam;
            log.info("Processing beacon request with token parameter, token length: {}", token.length());
        } else {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No token found in beacon request - no token parameter and no Authorization header");
                filterChain.doFilter(request, response);
                return;
            }
            token = authHeader.substring(7);
            log.info("Processing regular request with Authorization header, token length: {}", token.length());
        }

        try {
            userEmail = jwtService.extractUsername(token);
            log.info("Extracted user email from token: {}", userEmail);
        } catch (Exception e) {
            log.warn("Failed to extract username from token: {}", e.getMessage(), e);
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
                log.info("Successfully authenticated beacon request for user: {} (email: {})", 
                        ((UserDetails) authToken.getPrincipal()).getUsername(), userEmail);
            } else {
                log.warn("Token validation failed for user: {}", userEmail);
            }
        } else {
            log.info("User already authenticated or no user email: {}", userEmail);
        }
        
        filterChain.doFilter(request, response);
    }
}
