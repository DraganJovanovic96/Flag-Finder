package com.flagfinder.service;

import com.flagfinder.dto.*;
import com.flagfinder.model.Game;
import com.flagfinder.model.Round;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GameService {
    /**
     * Gets a game by ID
     */
    Game getGame(UUID gameId);
    
    /**
     * Gets all games for a user
     */
    List<CompletedGameDto> getGamesByUser();
    
    /**
     * Gets all games for a user with pagination
     */
    Page<CompletedGameDto> getGamesByUser(Integer page, Integer pageSize);
    
    /**
     * Gets count of won games for authenticated user
     */
    Long getWonGamesCount();
    
    /**
     * Gets count of draw games for authenticated user
     */
    Long getDrawGamesCount();
    
    /**
     * Gets all completed games
     */
    List<Game> getAllCompletedGames();
    
    /**
     * Starts a new game from a room with 2 players
     */
    GameDto startGame(UUID roomId, java.util.List<com.flagfinder.enumeration.Continent> continents);

    SinglePlayerGameDto startSinglePlayerGame(UUID roomId, java.util.List<com.flagfinder.enumeration.Continent> continents);
    
    /**
     * Submits a guess for the current round
     */
    GuessResponseDto submitGuess(GuessRequestDto guessRequest);
    
    /**
     * Gets current game state
     */
    GameDto getGameState(UUID gameId);
    
    /**
     * Ends the current game and calculates winner
     */
    GameDto endGame(UUID gameId);
    
    /**
     * Gets all rounds with guesses for a specific game
     */
    List<Round> getGameRounds(UUID gameId);
    
    /**
     * Gets all rounds with guesses for a specific game as DTOs
     */
    List<RoundSummaryDto> getGameRoundSummaries(UUID gameId);

    Long countOfWinningGames(String userName);

    int accuracyPercentage (String userName);

    /**
     * Gets single player game by room ID
     */
    SinglePlayerGameDto getSinglePlayerGameByRoom(UUID roomId);

    /**
     * Gets single player game by game ID
     */
    SinglePlayerGameDto getSinglePlayerGameById(UUID gameId);
}
