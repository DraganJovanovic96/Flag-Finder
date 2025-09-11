package com.flagfinder.controller;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.model.User;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gamename")
@RequiredArgsConstructor
public class GameNameController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/set")
    public ResponseEntity<?> setGameName(@RequestBody Map<String, String> request, Authentication authentication) {
        String gameName = request.get("gameName");
        
        if (gameName == null || gameName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Game name cannot be empty"));
        }
        
        gameName = gameName.trim();
        
        if (gameName.length() < 3 || gameName.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Game name must be between 3 and 20 characters"));
        }
        
        if (!gameName.matches("^[a-zA-Z0-9_]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Game name can only contain letters, numbers, and underscores"));
        }
        
        String userEmail = authentication.getName();
        
        // Check if gameName is taken by another user (exclude current user)
        var existingUser = userRepository.findOneByGameNameIgnoreCase(gameName);
        if (existingUser.isPresent() && !existingUser.get().getEmail().equals(userEmail)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Game name is already taken"));
        }
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setGameName(gameName);
        user.setInitialSetupCompleted(true);
        userRepository.save(user);
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("gameName", user.getGameName());
        
        String jwtToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return ResponseEntity.ok(AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }
    
    @GetMapping("/check/{gameName}")
    public ResponseEntity<?> checkGameNameAvailability(@PathVariable String gameName) {
        System.out.println("DEBUG - Checking availability for gameName: '" + gameName + "'");
        var existingUser = userRepository.findOneByGameNameIgnoreCase(gameName);
        System.out.println("DEBUG - Existing user found: " + existingUser.isPresent());
        if (existingUser.isPresent()) {
            System.out.println("DEBUG - Existing user gameName: '" + existingUser.get().getGameName() + "'");
            System.out.println("DEBUG - Existing user email: '" + existingUser.get().getEmail() + "'");
        }
        
        // Available if no user has this name, or if the current user already has this name
        boolean available = existingUser.isEmpty();
        System.out.println("DEBUG - GameName available: " + available);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
