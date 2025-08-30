package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GuessRequestDto {
    private UUID gameId;
    private Integer roundNumber;
    private String guessedCountryName;
}
