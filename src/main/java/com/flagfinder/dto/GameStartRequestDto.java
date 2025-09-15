package com.flagfinder.dto;

import com.flagfinder.enumeration.Continent;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO for starting a game request.
 * Contains the room ID and selected continents for the game.
 */
@Data
public class GameStartRequestDto {
    /**
     * The unique identifier of the room where the game will be started.
     */
    private UUID roomId;
    
    /**
     * List of continents/regions to include in the game.
     */
    private List<Continent> continents;
}
