package com.flagfinder.service;

import com.flagfinder.dto.*;
import com.flagfinder.model.Game;
import com.flagfinder.model.Round;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for game operations.
 * Provides methods for managing multiplayer and single player games, including game lifecycle,
 * guess submission, statistics calculation, and game state management.
 */
public interface GameService {
    /**
     * Gets a game by its unique identifier.
     *
     * @param gameId the UUID of the game
     * @return the Game entity
     * @throws RuntimeException if game not found
     */
    Game getGame(UUID gameId);
    
    /**
     * Gets all completed games for the authenticated user.
     *
     * @return list of completed game DTOs for the user
     */
    List<CompletedGameDto> getGamesByUser();
    
    /**
     * Gets all completed games for the authenticated user with pagination.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return paginated list of completed game DTOs
     */
    Page<CompletedGameDto> getGamesByUser(Integer page, Integer pageSize);
    
    /**
     * Gets the count of games won by the authenticated user.
     *
     * @return number of games won by the user
     */
    Long getWonGamesCount();
    
    /**
     * Gets the count of games that ended in a draw for the authenticated user.
     *
     * @return number of draw games for the user
     */
    Long getDrawGamesCount();
    
    /**
     * Gets all completed games in the system.
     *
     * @return list of all completed games
     */
    List<Game> getAllCompletedGames();
    
    /**
     * Starts a new multiplayer game from a room with 2 players.
     *
     * @param roomId the UUID of the room to start the game from
     * @param continents list of continents to include in the game
     * @return the started game DTO
     * @throws RuntimeException if room not found or game start fails
     */
    GameDto startGame(UUID roomId, java.util.List<com.flagfinder.enumeration.Continent> continents);

    /**
     * Starts a new single player game from a room.
     *
     * @param roomId the UUID of the single player room
     * @param continents list of continents to include in the game
     * @return the started single player game DTO
     * @throws RuntimeException if room not found or game start fails
     */
    SinglePlayerGameDto startSinglePlayerGame(UUID roomId, java.util.List<com.flagfinder.enumeration.Continent> continents);
    
    /**
     * Submits a guess for the current round.
     *
     * @param guessRequest the guess request containing game, round, and country information
     * @return the guess response with correctness and scoring information
     * @throws RuntimeException if guess submission fails
     */
    GuessResponseDto submitGuess(GuessRequestDto guessRequest);
    
    /**
     * Gets the current state of a game.
     *
     * @param gameId the UUID of the game
     * @return the current game state DTO
     * @throws RuntimeException if game not found
     */
    GameDto getGameState(UUID gameId);
    
    /**
     * Ends the current game and calculates the winner.
     *
     * @param gameId the UUID of the game to end
     * @return the final game state DTO with winner information
     * @throws RuntimeException if game not found or end fails
     */
    GameDto endGame(UUID gameId);
    
    /**
     * Gets all rounds with guesses for a specific game.
     *
     * @param gameId the UUID of the game
     * @return list of rounds with guess information
     * @throws RuntimeException if game not found
     */
    List<Round> getGameRounds(UUID gameId);
    
    /**
     * Gets all rounds with guesses for a specific game as DTOs.
     *
     * @param gameId the UUID of the game
     * @return list of round summary DTOs
     * @throws RuntimeException if game not found
     */
    List<RoundSummaryDto> getGameRoundSummaries(UUID gameId);

    /**
     * Counts the total number of winning games for a specific user.
     *
     * @param userName the username to count wins for
     * @return the number of games won by the user
     */
    Long countOfWinningGames(String userName);

    /**
     * Calculates the accuracy percentage for a specific user.
     *
     * @param userName the username to calculate accuracy for
     * @return the accuracy percentage (0-100)
     */
    int accuracyPercentage(String userName);

    /**
     * Gets the best winning streak for a specific user.
     *
     * @param userName the username to get the streak for
     * @return the best consecutive wins streak
     */
    int getBestWinningStreak(String userName);

    /**
     * Gets a single player game by its associated room ID.
     *
     * @param roomId the UUID of the single player room
     * @return the single player game DTO
     * @throws RuntimeException if game not found
     */
    SinglePlayerGameDto getSinglePlayerGameByRoom(UUID roomId);

    /**
     * Gets a single player game by its unique identifier.
     *
     * @param gameId the UUID of the single player game
     * @return the single player game DTO
     * @throws RuntimeException if game not found
     */
    SinglePlayerGameDto getSinglePlayerGameById(UUID gameId);
}
