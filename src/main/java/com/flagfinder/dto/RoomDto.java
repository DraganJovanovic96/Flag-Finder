package com.flagfinder.dto;

import com.flagfinder.enumeration.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO representing a multiplayer game room.
 * Contains information about the room participants, status, and game timing.
 */
@Data
public class RoomDto extends BaseEntityDto {
    /**
     * The username of the host player who created the room.
     */
    private String hostUserName;
    
    /**
     * The username of the guest player who joined the room.
     */
    private String guestUserName;
    
    /**
     * The current status of the room.
     */
    private RoomStatus status;
    
    /**
     * The number of rounds to be played in the game.
     */
    private Integer numberOfRounds;
    
    /**
     * The date and time when the game started.
     */
    private LocalDateTime gameStartedAt;
    
    /**
     * The date and time when the game ended.
     */
    private LocalDateTime gameEndedAt;
}
