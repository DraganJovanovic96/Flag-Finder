package com.flagfinder.dto;

import com.flagfinder.enumeration.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * DTO representing a single player game room.
 * Contains room information for single player games including host details and timing.
 */
@Data
public class SinglePlayerRoomDto extends BaseEntityDto{
    /**
     * The username of the host player (single player).
     */
    private String hostUserName;
    
    /**
     * The current status of the single player room.
     */
    private RoomStatus status;
    
    /**
     * The number of rounds to be played in the single player game.
     */
    private Integer numberOfRounds;
    
    /**
     * The date and time when the single player game started.
     */
    private LocalDateTime gameStartedAt;
    
    /**
     * The date and time when the single player game ended.
     */
    private LocalDateTime gameEndedAt;
}
