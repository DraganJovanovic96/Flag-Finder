package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for creating a new multiplayer room request.
 * Contains configuration parameters for the room setup.
 */
@Data
public class CreateRoomRequestDto {
    /**
     * The number of rounds to be played in the game.
     * Defaults to 5 rounds if not specified.
     */
    private Integer numberOfRounds = 5;
}
