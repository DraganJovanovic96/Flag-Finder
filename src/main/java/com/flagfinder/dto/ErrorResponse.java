package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing an error response.
 * Used to provide structured error information to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /**
     * The HTTP status code of the error.
     */
    private int status;
    
    /**
     * The error message describing what went wrong.
     */
    private String message;
    
    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;
}
