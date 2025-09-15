package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for submitting a guess in a game.
 * Contains the game context and the player's guess.
 */
@Data
public class GuessRequestDto {
    /**
     * The unique identifier of the game where the guess is being made.
     */
    private UUID gameId;
    
    /**
     * The round number in which the guess is being made.
     */
    private Integer roundNumber;
    
    /**
     * The name of the country being guessed.
     */
    private String guessedCountryName;
}
