package com.flagfinder.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoundSummaryDto {
    private Integer roundNumber;
    private CountryDto country;
    private List<GuessDto> guesses;
}
