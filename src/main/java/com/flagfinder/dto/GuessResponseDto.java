package com.flagfinder.dto;

import lombok.Data;

@Data
public class GuessResponseDto {
    private GameDto game;
    private boolean correct;
    private String message;
    private Integer pointsAwarded;
    private String correctCountryName;
}
