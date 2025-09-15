package com.flagfinder.dto;

import lombok.Data;

@Data
public class CreateRoomRequestDto {
    private Integer numberOfRounds = 5; // Default to 5 rounds
}
