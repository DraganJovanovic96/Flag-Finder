package com.flagfinder.service;

import java.util.UUID;

public interface GameTimerService {
    
    /**
     * Starts a timer for a specific round in a game
     */
    void startRoundTimer(UUID gameId, Integer roundNumber, int durationSeconds);
    
    /**
     * Cancels all timers for a specific game
     */
    void cancelGameTimers(UUID gameId);
    
    /**
     * Gets remaining time for current round
     */
    Long getRemainingTime(UUID gameId, Integer roundNumber);
    
    /**
     * Checks if a round is still active
     */
    boolean isRoundActive(UUID gameId, Integer roundNumber);
}
