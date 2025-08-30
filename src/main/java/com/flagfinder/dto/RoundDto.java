package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class RoundDto {
    private UUID id;
    private Integer roundNumber;
    private String countryName;
    private UUID countryId;
    private byte[] flagImage;
    private Long timeRemaining;
    private boolean roundActive;
}
