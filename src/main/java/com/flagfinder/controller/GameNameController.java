package com.flagfinder.controller;

import com.flagfinder.service.GameNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gamename")
@RequiredArgsConstructor
public class GameNameController {

    private final GameNameService gameNameService;

    @PostMapping("/set")
    public ResponseEntity<?> setGameName(@RequestBody Map<String, String> request, Authentication authentication) {
        return gameNameService.setGameName(request, authentication);
    }
    
    @GetMapping("/check/{gameName}")
    public ResponseEntity<?> checkGameNameAvailability(@PathVariable String gameName) {
        return gameNameService.checkGameNameAvailability(gameName);
    }
}
