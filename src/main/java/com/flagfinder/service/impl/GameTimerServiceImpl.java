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

@Service
@RequiredArgsConstructor
@Slf4j
public class GameTimerServiceImpl implements GameTimerService {
    
    private final ApplicationContext applicationContext;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> roundStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> roundDurations = new ConcurrentHashMap<>();
    
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
                log.error("Error handling round timeout", e);
            }
            cleanupRoundTimer(timerKey);
        }, durationSeconds, TimeUnit.SECONDS);
        
        activeTimers.put(timerKey, future);
    }
    
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
    
    @Override
    public boolean isRoundActive(UUID gameId, Integer roundNumber) {
        String timerKey = getTimerKey(gameId, roundNumber);
        ScheduledFuture<?> future = activeTimers.get(timerKey);
        
        return future != null && !future.isDone() && !future.isCancelled();
    }
    
    private String getTimerKey(UUID gameId, Integer roundNumber) {
        return gameId.toString() + "_" + roundNumber;
    }
    
    private void cancelRoundTimer(String timerKey) {
        ScheduledFuture<?> existingTimer = activeTimers.remove(timerKey);
        if (existingTimer != null) {
            existingTimer.cancel(false);
        }
        cleanupRoundTimer(timerKey);
    }
    
    private void cleanupRoundTimer(String timerKey) {
        roundStartTimes.remove(timerKey);
        roundDurations.remove(timerKey);
        activeTimers.remove(timerKey);
    }
}
