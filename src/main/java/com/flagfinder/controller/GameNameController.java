package com.flagfinder.controller;

import com.flagfinder.service.GameNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing game name operations.
 * Provides endpoints for setting and checking game name availability.
 */
@RestController
@RequestMapping("/api/v1/gamename")
@RequiredArgsConstructor
public class GameNameController {

    private final GameNameService gameNameService;

    /**
     * Sets a game name for the authenticated user.
     *
     * @param request the request containing the game name
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity with the result of the operation
     */
    @PostMapping("/set")
    public ResponseEntity<?> setGameName(@RequestBody Map<String, String> request, Authentication authentication) {
        return gameNameService.setGameName(request, authentication);
    }
    
    /**
     * Checks if a game name is available.
     *
     * @param gameName the game name to check for availability
     * @return a ResponseEntity indicating whether the game name is available
     */
    @GetMapping("/check/{gameName}")
    public ResponseEntity<?> checkGameNameAvailability(@PathVariable String gameName) {
        return gameNameService.checkGameNameAvailability(gameName);
    }
}
