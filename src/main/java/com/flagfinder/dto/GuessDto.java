package com.flagfinder.dto;

import lombok.Data;

@Data
public class GuessDto {
    private String userGameName;
    private String guessedCountryName;
    private String guessedCountryId;
    private boolean correct;
}
