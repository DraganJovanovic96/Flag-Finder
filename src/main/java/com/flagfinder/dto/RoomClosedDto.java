package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for room closure notifications.
 * Contains information about the closed room and a message explaining the closure.
 */
@Data
public class RoomClosedDto {
    /**
     * The unique identifier of the room that was closed.
     */
    private String roomId;
    
    /**
     * A message explaining why the room was closed.
     */
    private String message;
}
