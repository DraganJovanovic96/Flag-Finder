package com.flagfinder.controller;

import com.flagfinder.dto.*;
import com.flagfinder.model.Game;
import com.flagfinder.service.GameService;
import com.flagfinder.service.impl.HelperMethods;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    private final HelperMethods helperMethods;

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
        GameDto game = gameService.startGame(request.getRoomId(), request.getContinents());

        return ResponseEntity.status(HttpStatus.CREATED).body(game);
    }

    /**
     * Starts a new game from a room with exactly 2 players and returns a ResponseEntity object with status code 201 (Created)
     * and the created GameDto object in the response body.
     *
     * @param request the DTO containing the room ID from which to start the game
     * @return a ResponseEntity object with status code 201 (Created) and the created GameDto object in the response body
     * @throws ResponseStatusException if the room is not found or doesn't have exactly 2 players
     */
    @PostMapping("/start-single-player-game")
    public ResponseEntity<SinglePlayerGameDto> startSinglePlayerGame(@RequestBody GameStartRequestDto request) {
        SinglePlayerGameDto singlePlayerGameDto = gameService.startSinglePlayerGame(request.getRoomId(), request.getContinents());

        return ResponseEntity.status(HttpStatus.CREATED).body(singlePlayerGameDto);
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
    public ResponseEntity<GuessResponseDto> submitGuess(@RequestBody GuessRequestDto guessRequest) {
        GuessResponseDto response = gameService.submitGuess(guessRequest);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> getGameState(@PathVariable UUID gameId) {
        try {
            GameDto game = gameService.getGameState(gameId);
            return ResponseEntity.ok(game);
        } catch (ResponseStatusException e) {
            try {
                SinglePlayerGameDto singlePlayerGame = gameService.getSinglePlayerGameById(gameId);
                return ResponseEntity.ok(singlePlayerGame);
            } catch (ResponseStatusException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
            }
        }
    }

    /**
     * Retrieves the current state of a single player game by room ID and returns a ResponseEntity object with status code 200 (OK)
     * and the SinglePlayerGameDto object in the response body.
     *
     * @param roomId the unique UUID identifier of the room to retrieve the game for
     * @return a ResponseEntity object with status code 200 (OK) and the SinglePlayerGameDto object in the response body
     * @throws ResponseStatusException if the game is not found
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<SinglePlayerGameDto> getSinglePlayerGameByRoom(@PathVariable UUID roomId) {
        SinglePlayerGameDto game = gameService.getSinglePlayerGameByRoom(roomId);

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
     * @return a ResponseEntity object with status code 200 (OK) and the list of Game objects in the response body
     */
    @GetMapping("/user/game-history")
    public ResponseEntity<List<CompletedGameDto>> getGamesByUser() {
        List<CompletedGameDto> games = gameService.getGamesByUser();

        return ResponseEntity.ok(games);
    }
    
    /**
     * Retrieves paginated games for a specific user and returns a ResponseEntity object with status code 200 (OK)
     * and the paginated Game objects in the response body.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return a ResponseEntity object with status code 200 (OK) and the paginated Game objects in the response body
     */
    @GetMapping("/user/game-history/paginated")
    public ResponseEntity<Page<CompletedGameDto>> getGamesByUserPaginated(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "5") @Min(1) int pageSize) {
        Page<CompletedGameDto> games = gameService.getGamesByUser(page, pageSize);

        return ResponseEntity.ok(games);
    }
    
    /**
     * Retrieves count of won games for the authenticated user.
     *
     * @return a ResponseEntity object with status code 200 (OK) and the count of won games
     */
    @GetMapping("/user/won-games-count")
    public ResponseEntity<Long> getWonGamesCount() {
        Long wonGamesCount = gameService.getWonGamesCount();
        return ResponseEntity.ok(wonGamesCount);
    }
    
    /**
     * Retrieves count of draw games for the authenticated user.
     *
     * @return a ResponseEntity object with status code 200 (OK) and the count of draw games
     */
    @GetMapping("/user/draw-games-count")
    public ResponseEntity<Long> getDrawGamesCount() {
        Long drawGamesCount = gameService.getDrawGamesCount();
        return ResponseEntity.ok(drawGamesCount);
    }
    
    /**
     * Retrieves all rounds with guesses for a specific game by game ID and returns a ResponseEntity object with status code 200 (OK)
     * and the list of RoundSummaryDto objects with their guesses in the response body.
     *
     * @param gameId the unique UUID identifier of the game whose rounds to retrieve
     * @return a ResponseEntity object with status code 200 (OK) and the list of RoundSummaryDto objects with guesses in the response body
     * @throws ResponseStatusException if the game is not found
     */
    @GetMapping("/{gameId}/rounds")
    public ResponseEntity<List<RoundSummaryDto>> getGameRounds(@PathVariable UUID gameId) {
        List<RoundSummaryDto> rounds = gameService.getGameRoundSummaries(gameId);

        return ResponseEntity.ok(rounds);
    }

    /**
     * Retrieves user information including winnings count for a specific user.
     *
     * @param sendUserNameDto the DTO containing the username to retrieve info for
     * @return a ResponseEntity object with status code 200 (OK) and the UserInfoDto object in the response body
     */
    @PostMapping("user/info")
    public ResponseEntity<UserInfoDto> getUserWinningsCount(@RequestBody SendUserNameDto sendUserNameDto) {

        return  ResponseEntity.ok(helperMethods.setPlayerInfoCard(sendUserNameDto.getUserName()));
    }
}
