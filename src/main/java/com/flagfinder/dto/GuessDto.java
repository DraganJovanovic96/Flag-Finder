package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO representing a player's guess in a game.
 * Contains information about the user, their guess, and whether it was correct.
 */
@Data
public class GuessDto {
    /**
     * The game name/username of the player who made the guess.
     */
    private String userGameName;
    
    /**
     * The name of the country that was guessed.
     */
    private String guessedCountryName;
    
    /**
     * The unique identifier of the guessed country.
     */
    private String guessedCountryId;
    
    /**
     * Indicates whether the guess was correct.
     */
    private boolean correct;
}
