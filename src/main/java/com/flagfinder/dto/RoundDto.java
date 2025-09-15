package com.flagfinder.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing a game round in multiplayer mode.
 * Contains round information, target country, timing, and player guesses.
 */
@Data
public class RoundDto {
    /**
     * The unique identifier of the round.
     */
    private UUID id;
    
    /**
     * The sequential number of this round in the game.
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
     * List of guesses made by players in this round.
     */
    private List<GuessDto> guesses;
}
