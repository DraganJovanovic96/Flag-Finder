package com.flagfinder.dto;

import com.flagfinder.enumeration.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a single player game.
 * Contains game information, player details, scoring, and current round data.
 */
@Data
public class SinglePlayerGameDto {
    /**
     * The unique identifier of the single player game.
     */
    private UUID id;
    
    /**
     * The unique identifier of the room associated with this game.
     */
    private UUID roomId;
    
    /**
     * The name of the player playing the game.
     */
    private String playerName;
    
    /**
     * The name of the host (same as player in single player mode).
     */
    private String hostName;
    
    /**
     * The current score of the host/player.
     */
    private Integer hostScore;
    
    /**
     * The total number of rounds in the game.
     */
    private Integer totalRounds;
    
    /**
     * The current round number being played.
     */
    private Integer currentRound;
    
    /**
     * The current status of the game.
     */
    private GameStatus status;
    
    /**
     * The date and time when the game started.
     */
    private LocalDateTime startedAt;
    
    /**
     * The date and time when the game ended.
     */
    private LocalDateTime endedAt;
    
    /**
     * The current round data for the active round.
     */
    private SinglePlayerRoundDto currentSinglePlayerRoundData;
}
