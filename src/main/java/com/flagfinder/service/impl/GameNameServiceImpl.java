package com.flagfinder.service.impl;

import com.flagfinder.dto.AuthenticationResponseDto;
import com.flagfinder.model.User;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.GameNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameNameServiceImpl implements GameNameService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<?> setGameName(Map<String, String> request, Authentication authentication) {
        try {
            String gameName = request.get("gameName");
            String userEmail = authentication.getName();
            
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @Override
    public ResponseEntity<?> checkGameNameAvailability(String gameName) {
        var existingUser = userRepository.findOneByGameNameIgnoreCase(gameName);
        boolean available = existingUser.isEmpty();
        return ResponseEntity.ok(Map.of("available", available));
    }
}
