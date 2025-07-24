package com.flagfinder.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * GlobalExceptionHandler is a global exception handler for handling exceptions
 * across the entire application. It is annotated with @ControllerAdvice,
 * which allows it to intercept exceptions thrown by any controller.
 * <p>
 * This class specifically handles ResponseStatusException and returns appropriate
 * HTTP status codes and messages.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResponseStatusException thrown by controllers or service layers.
     * This method captures the exception and returns a response with the HTTP
     * status code and reason provided by the exception.
     *
     * @param ex the ResponseStatusException thrown by the application
     * @return the reason message of the exception, if it matches the expected status
     */
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        // Return the response with the correct HTTP status and message
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }
}
