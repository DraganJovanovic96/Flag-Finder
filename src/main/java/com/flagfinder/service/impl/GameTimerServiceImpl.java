package com.flagfinder.service.impl;

import com.flagfinder.service.GameTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of GameTimerService interface.
 * Provides comprehensive timer management for game rounds using scheduled executors.
 * Manages round timers, tracks remaining time, and handles automatic round progression.
 * Uses concurrent data structures for thread-safe timer operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameTimerServiceImpl implements GameTimerService {
    
    private final ApplicationContext applicationContext;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> roundStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> roundDurations = new ConcurrentHashMap<>();
    
    /**
     * Starts a timer for a specific round in a game.
     * Cancels any existing timer for the same round and creates a new scheduled task.
     * When the timer expires, it triggers automatic round progression.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to start the timer for
     * @param durationSeconds the duration of the timer in seconds
     */
    @Override
    public void startRoundTimer(UUID gameId, Integer roundNumber, int durationSeconds) {
        String timerKey = getTimerKey(gameId, roundNumber);
        
        cancelRoundTimer(timerKey);
        
        roundStartTimes.put(timerKey, LocalDateTime.now());
        roundDurations.put(timerKey, durationSeconds);
        
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                GameServiceImpl gameService = applicationContext.getBean(GameServiceImpl.class);
                gameService.handleRoundTimeout(gameId, roundNumber);
            } catch (Exception e) {
            }
            cleanupRoundTimer(timerKey);
        }, durationSeconds, TimeUnit.SECONDS);
        
        activeTimers.put(timerKey, future);
    }
    
    /**
     * Cancels all active timers for a specific game.
     * Removes all timer-related data and cancels scheduled tasks.
     *
     * @param gameId the UUID of the game to cancel timers for
     */
    @Override
    public void cancelGameTimers(UUID gameId) {
        String gamePrefix = gameId.toString() + "_";
        
        activeTimers.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(gamePrefix)) {
                entry.getValue().cancel(false);
                roundStartTimes.remove(entry.getKey());
                roundDurations.remove(entry.getKey());
                
                return true;
            }
            return false;
        });
    }
    
    /**
     * Calculates the remaining time for a specific round timer.
     * Returns the time left in seconds, or 0 if timer doesn't exist or has expired.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to check
     * @return the remaining time in seconds, or 0 if timer not found
     */
    @Override
    public Long getRemainingTime(UUID gameId, Integer roundNumber) {
        String timerKey = getTimerKey(gameId, roundNumber);
        
        LocalDateTime startTime = roundStartTimes.get(timerKey);
        Integer duration = roundDurations.get(timerKey);
        
        if (startTime == null || duration == null) {
            return 0L;
        }
        
        long elapsedSeconds = java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
        long remaining = duration - elapsedSeconds;
        
        return Math.max(0L, remaining);
    }
    
    /**
     * Checks if a round timer is currently active and running.
     * Returns true if the timer exists and hasn't completed or been cancelled.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number to check
     * @return true if the round timer is active, false otherwise
     */
    @Override
    public boolean isRoundActive(UUID gameId, Integer roundNumber) {
        String timerKey = getTimerKey(gameId, roundNumber);
        ScheduledFuture<?> future = activeTimers.get(timerKey);
        
        return future != null && !future.isDone() && !future.isCancelled();
    }
    
    /**
     * Generates a unique timer key for a specific game and round combination.
     *
     * @param gameId the UUID of the game
     * @param roundNumber the round number
     * @return a unique string key for the timer
     */
    private String getTimerKey(UUID gameId, Integer roundNumber) {
        return gameId.toString() + "_" + roundNumber;
    }
    
    /**
     * Cancels a specific round timer by its key.
     * Removes the timer from active timers and cleans up associated data.
     *
     * @param timerKey the unique key of the timer to cancel
     */
    private void cancelRoundTimer(String timerKey) {
        ScheduledFuture<?> existingTimer = activeTimers.remove(timerKey);
        if (existingTimer != null) {
            existingTimer.cancel(false);
        }
        cleanupRoundTimer(timerKey);
    }
    
    /**
     * Cleans up all data associated with a specific timer.
     * Removes start times, durations, and active timer references.
     *
     * @param timerKey the unique key of the timer to clean up
     */
    private void cleanupRoundTimer(String timerKey) {
        roundStartTimes.remove(timerKey);
        roundDurations.remove(timerKey);
        activeTimers.remove(timerKey);
    }
}
