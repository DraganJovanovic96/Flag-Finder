package com.flagfinder.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing a round in single player mode.
 * Contains round information, target country, timing, and the player's guess.
 */
@Data
public class SinglePlayerRoundDto {
    /**
     * The unique identifier of the single player round.
     */
    private UUID id;
    
    /**
     * The sequential number of this round in the single player game.
     */
    private Integer roundNumber;
    
    /**
     * The name of the target country for this round.
     */
    private String countryName;
    
    /**
     * The unique identifier of the target country.
     */
    private UUID countryId;
    
    /**
     * The flag image data as byte array.
     */
    private byte[] flagImage;
    
    /**
     * The remaining time for this round in milliseconds.
     */
    private Long timeRemaining;
    
    /**
     * Indicates whether this round is currently active.
     */
    private boolean roundActive;
    
    /**
     * The guess made by the player in this round.
     */
    private GuessDto guess;
}
