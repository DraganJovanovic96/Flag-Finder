package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for creating a new single player room request.
 * Contains configuration parameters for the single player game setup.
 */
@Data
public class CreateSinglePlayerRoomRequestDto {
    /**
     * The number of rounds to be played in the single player game.
     * Defaults to 5 rounds if not specified.
     */
    private Integer numberOfRounds = 5;
}
