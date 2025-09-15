package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for updating the number of rounds in a room.
 * Contains the new number of rounds to be set.
 */
@Data
public class UpdateRoundsRequestDto {
    /**
     * The new number of rounds to be set for the room.
     */
    private int numberOfRounds;
}
