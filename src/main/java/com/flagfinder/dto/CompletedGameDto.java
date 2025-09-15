package com.flagfinder.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a completed multiplayer game.
 * Contains comprehensive game results including player information, scores, rounds, and timing.
 */
@Data
public class CompletedGameDto {
    /**
     * The unique identifier of the room where the game was played.
     */
    private UUID roomId;
    
    /**
     * The username of the host player.
     */
    private String hostUserName;
    
    /**
     * The username of the guest player.
     */
    private String guestUserName;
    
    /**
     * The username of the winning player.
     */
    private String winnerUserName;
    
    /**
     * The final score achieved by the host player.
     */
    private Integer hostScore;
    
    /**
     * The final score achieved by the guest player.
     */
    private Integer guestScore;
    
    /**
     * List of all rounds played in the game.
     */
    private List<RoundDto> roundDtos;
    
    /**
     * The date and time when the game started.
     */
    private LocalDateTime startedAt;
    
    /**
     * The total number of rounds played in the game.
     */
    private Integer totalRounds;
    
    /**
     * The date and time when the game ended.
     */
    private LocalDateTime endedAt;
}
