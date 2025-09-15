package com.flagfinder.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for summarizing a completed round.
 * Contains round information, target country, and all guesses made during the round.
 */
@Data
public class RoundSummaryDto {
    /**
     * The sequential number of this round in the game.
     */
    private Integer roundNumber;
    
    /**
     * The target country for this round.
     */
    private CountryDto country;
    
    /**
     * List of all guesses made by players during this round.
     */
    private List<GuessDto> guesses;
}
