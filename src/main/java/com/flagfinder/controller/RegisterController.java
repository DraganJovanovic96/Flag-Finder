package com.flagfinder.controller;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.dto.RegisterRequestDto;
import com.flagfinder.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

/**
 * Controller for handling user registration.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/register")
@CrossOrigin
public class RegisterController {
    /**
     * The service used to for authentication.
     */
    private final AuthenticationService service;

    /**
     * Registers a new user.
     *
     * @param request the registration request containing user details
     * @return the ResponseEntity containing the authentication response
     */
    @PostMapping
    public ResponseEntity<AuthenticationResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) throws UnsupportedEncodingException {
        return ResponseEntity.ok(service.register(request));
    }
}
