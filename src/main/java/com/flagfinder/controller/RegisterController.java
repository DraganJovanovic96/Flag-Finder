package com.flagfinder.controller;

import com.mss.dto.AuthenticationResponseDto;
import com.mss.dto.RegisterRequestDto;
import com.mss.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/register")
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
    @PreAuthorize("hasAnyAuthority('admin:create')")
    public ResponseEntity<AuthenticationResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request
    ) throws UnsupportedEncodingException {
        return ResponseEntity.ok(service.register(request));
    }
}
