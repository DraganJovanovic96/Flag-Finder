package com.flagfinder.controller;

import com.flagfinder.model.Game;
import com.flagfinder.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;

    /**
     * Gets a game by ID
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(game);
    }
    
    /**
     * Gets all games for a user
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<Game>> getGamesByUser(@PathVariable String userName) {
        List<Game> games = gameService.getGamesByUser(userName);
        return ResponseEntity.ok(games);
    }
} 