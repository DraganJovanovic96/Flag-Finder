package com.flagfinder.config;


import com.flagfinder.repository.TokenRepository;
import com.flagfinder.service.impl.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JwtAuthenticationFilter is a component that intercepts incoming requests and performs JWT authentication.
 * It extends OncePerRequestFilter to ensure it is executed only once per request.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * Service used to retrieve JWT data.
     */
    private final JwtService jwtService;

    /**
     * Service used to retrieve User details data.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Repository used to retrieve token data.
     */
    private final TokenRepository tokenRepository;

    /**
     * Performs the JWT authentication process by validating the JWT token and setting the authentication
     * information in the SecurityContextHolder.
     *
     * @param request     The HttpServletRequest object representing the incoming request.
     * @param response    The HttpServletResponse object representing the outgoing response.
     * @param filterChain The FilterChain object to proceed with the request processing.
     * @throws ServletException If an error occurs during the filter processing.
     * @throws IOException      If an I/O error occurs during the filter processing.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        String userEmail1;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail1 = null;

        try {
            userEmail1 = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token expired: " + e.getMessage());
            return;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid token: " + e.getMessage());
            return;
        }

        userEmail = userEmail1;
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            var tokenOpt = tokenRepository.findByToken(jwt);

            if (tokenOpt.isPresent()) {
                var isTokenValid = !tokenOpt.get().isExpired() && !tokenOpt.get().isRevoked();

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("Token is either revoked or invalid.");
                    return;
                }
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid token.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
