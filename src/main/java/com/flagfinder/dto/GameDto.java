package com.flagfinder.dto;

import com.flagfinder.enumeration.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing a game.
 * Contains all game-related information including players, scores, and status.
 */
@Data
public class GameDto {
    /**
     * Unique identifier for the game.
     */
    private UUID id;
    
    /**
     * Identifier of the room where the game is played.
     */
    private UUID roomId;
    
    /**
     * List of player names participating in the game.
     */
    private List<String> playerNames;
    
    /**
     * Name of the host player.
     */
    private String hostName;
    
    /**
     * Name of the guest player.
     */
    private String guestName;
    
    /**
     * Current score of the host player.
     */
    private Integer hostScore;
    
    /**
     * Current score of the guest player.
     */
    private Integer guestScore;
    
    /**
     * Total number of rounds in the game.
     */
    private Integer totalRounds;
    
    /**
     * Current round number.
     */
    private Integer currentRound;
    
    /**
     * Current status of the game.
     */
    private GameStatus status;
    
    /**
     * Timestamp when the game started.
     */
    private LocalDateTime startedAt;
    
    /**
     * Timestamp when the game ended.
     */
    private LocalDateTime endedAt;
    
    /**
     * Username of the game winner.
     */
    private String winnerUserName;
    
    /**
     * Data for the current round.
     */
    private RoundDto currentRoundData;
}
