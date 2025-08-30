package com.flagfinder.controller;

import com.flagfinder.dto.GameDto;
import com.flagfinder.dto.GameStartRequestDto;
import com.flagfinder.dto.GuessRequestDto;
import com.flagfinder.model.Game;
import com.flagfinder.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Controller class for handling game-related API endpoints.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@CrossOrigin
public class GameController {
    
    /**
     * The service used for game operations.
     */
    private final GameService gameService;

    /**
     * Starts a new game from a room with exactly 2 players and returns a ResponseEntity object with status code 201 (Created)
     * and the created GameDto object in the response body.
     *
     * @param request the DTO containing the room ID from which to start the game
     * @return a ResponseEntity object with status code 201 (Created) and the created GameDto object in the response body
     * @throws ResponseStatusException if the room is not found or doesn't have exactly 2 players
     */
    @PostMapping("/start")
    public ResponseEntity<GameDto> startGame(@RequestBody GameStartRequestDto request) {
        GameDto game = gameService.startGame(request.getRoomId());
        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }
    
    /**
     * Submits a player's guess for the current round and returns a ResponseEntity object with status code 200 (OK)
     * and the updated GameDto object in the response body.
     *
     * @param guessRequest the DTO containing the game ID, round number, and guessed country name
     * @return a ResponseEntity object with status code 200 (OK) and the updated GameDto object in the response body
     * @throws ResponseStatusException if the game is not found, not in progress, or user already guessed in this round
     */
    @PostMapping("/guess")
    public ResponseEntity<GameDto> submitGuess(@RequestBody GuessRequestDto guessRequest) {
        GameDto game = gameService.submitGuess(guessRequest);
        return ResponseEntity.ok(game);
    }
    
    /**
     * Retrieves the current state of a game by its unique identifier and returns a ResponseEntity object with status code 200 (OK)
     * and the GameDto object in the response body.
     *
     * @param gameId the unique UUID identifier of the game to retrieve
     * @return a ResponseEntity object with status code 200 (OK) and the GameDto object in the response body
     * @throws ResponseStatusException if the game is not found
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGameState(@PathVariable UUID gameId) {
        GameDto game = gameService.getGameState(gameId);
        return ResponseEntity.ok(game);
    }
    
    /**
     * Ends a game manually and calculates the winner based on scores, returning a ResponseEntity object with status code 200 (OK)
     * and the final GameDto object in the response body.
     *
     * @param gameId the unique UUID identifier of the game to end
     * @return a ResponseEntity object with status code 200 (OK) and the final GameDto object in the response body
     * @throws ResponseStatusException if the game is not found
     */
    @PostMapping("/{gameId}/end")
    public ResponseEntity<GameDto> endGame(@PathVariable UUID gameId) {
        GameDto game = gameService.endGame(gameId);
        return ResponseEntity.ok(game);
    }
    
    /**
     * Retrieves all games for a specific user by username and returns a ResponseEntity object with status code 200 (OK)
     * and the list of Game objects in the response body.
     *
     * @param userName the username of the player whose games to retrieve
     * @return a ResponseEntity object with status code 200 (OK) and the list of Game objects in the response body
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<Game>> getGamesByUser(@PathVariable String userName) {
        List<Game> games = gameService.getGamesByUser(userName);
        return ResponseEntity.ok(games);
    }
}
