package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for the response after a player submits a guess.
 * Contains the game state, correctness of the guess, and feedback information.
 */
@Data
public class GuessResponseDto {
    /**
     * The current game object after the guess has been processed.
     */
    private Object game;
    
    /**
     * Indicates whether the guess was correct.
     */
    private boolean correct;
    
    /**
     * A message providing feedback about the guess.
     */
    private String message;
    
    /**
     * The number of points awarded for the guess.
     */
    private Integer pointsAwarded;
    
    /**
     * The name of the correct country if the guess was wrong.
     */
    private String correctCountryName;
}
