package com.flagfinder.controller;

import com.flagfinder.dto.UserProfileDto;
import com.flagfinder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user-related operations.
 * Provides endpoints for user profile management.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    /**
     * Retrieves user profile information by email address.
     * Used for mapping emails to display names in the frontend.
     *
     * @param email The email address of the user to retrieve profile for
     * @return ResponseEntity containing UserProfileDto with email and gameName
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(@RequestParam String email) {
        UserProfileDto userProfile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(userProfile);
    }
}
