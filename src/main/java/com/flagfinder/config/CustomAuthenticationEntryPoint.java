package com.flagfinder.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * CustomAuthenticationEntryPoint is an implementation of Spring Security's AuthenticationEntryPoint
 * interface. It handles the commencement of the authentication process when an unauthenticated user
 * attempts to access a secured resource. This class provides custom logic to respond to unauthorized
 * access requests.
 * <p>
 * In this implementation, if the request method is DELETE, it returns a "NOT_FOUND" response status
 * (HTTP 404) indicating that the resource is not found. For other HTTP methods, it sets the response
 * status to "UNAUTHORIZED" (HTTP 401) to indicate authentication failure.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Commences the authentication process when an unauthenticated user attempts to access a secured resource.
     *
     * @param request       The HttpServletRequest representing the incoming request.
     * @param response      The HttpServletResponse representing the response to be sent to the client.
     * @param authException An AuthenticationException that may have caused the authentication failure.
     * @throws IOException If an I/O error occurs while handling the response.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (request.getMethod().equals("DELETE")) {
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource is not found.");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
