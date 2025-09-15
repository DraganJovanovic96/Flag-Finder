package com.flagfinder.controller;

import com.flagfinder.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling OAuth2 authentication operations.
 * Manages OAuth2 success callbacks and user authentication flow.
 */
@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    /**
     * Handles successful OAuth2 authentication callback.
     * Processes user information from OAuth2 provider and completes authentication.
     *
     * @param email the user's email address from OAuth2 provider
     * @param name the user's full name from OAuth2 provider
     * @param googleId the user's Google ID
     * @param picture the user's profile picture URL (optional)
     * @param givenName the user's given name (optional)
     * @param familyName the user's family name (optional)
     * @param locale the user's locale preference (optional)
     * @param response the HTTP response object for redirecting user
     * @throws IOException if an I/O error occurs during response handling
     */
    @GetMapping("/success")
    public void oauth2Success(@RequestParam String email, 
                             @RequestParam String name, 
                             @RequestParam String googleId,
                             @RequestParam(required = false) String picture,
                             @RequestParam(required = false) String givenName,
                             @RequestParam(required = false) String familyName,
                             @RequestParam(required = false) String locale,
                             HttpServletResponse response) throws IOException {
        Map<String, String> userParams = new HashMap<>();
        userParams.put("email", email);
        userParams.put("name", name);
        userParams.put("googleId", googleId);
        userParams.put("picture", picture);
        userParams.put("givenName", givenName);
        userParams.put("familyName", familyName);
        userParams.put("locale", locale);
        
        oAuth2Service.handleOAuth2Success(userParams, response);
    }
}
