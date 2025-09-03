package com.flagfinder.service;

import com.flagfinder.dto.GameDto;
import com.flagfinder.dto.GuessRequestDto;
import com.flagfinder.dto.GuessResponseDto;
import com.flagfinder.dto.RoundSummaryDto;
import com.flagfinder.model.Game;
import com.flagfinder.model.Round;
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
    List<Game> getGamesByUser(String userName);
    
    /**
     * Gets all completed games
     */
    List<Game> getAllCompletedGames();
    
    /**
     * Starts a new game from a room with 2 players
     */
    GameDto startGame(UUID roomId, java.util.List<com.flagfinder.enumeration.Continent> continents);
    
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
}
