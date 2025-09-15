package com.flagfinder.service;

import java.util.UUID;

/**
 * Service interface for game timer operations.
 * Provides methods for managing round timers, tracking remaining time, and controlling game timing.
 */
public interface GameTimerService {
    
    /**
     * Starts a timer for a specific round in a game.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to start the timer for
     * @param durationSeconds the duration of the timer in seconds
     */
    void startRoundTimer(UUID gameId, Integer roundNumber, int durationSeconds);
    
    /**
     * Cancels all timers for a specific game.
     *
     * @param gameId the UUID of the game to cancel timers for
     */
    void cancelGameTimers(UUID gameId);
    
    /**
     * Gets the remaining time for the current round.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to check
     * @return the remaining time in seconds, or null if timer not found
     */
    Long getRemainingTime(UUID gameId, Integer roundNumber);
    
    /**
     * Checks if a round is still active (timer hasn't expired).
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to check
     * @return true if the round is still active, false otherwise
     */
    boolean isRoundActive(UUID gameId, Integer roundNumber);
}
