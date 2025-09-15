package com.flagfinder.dto;

import lombok.Data;

@Data
public class CreateSinglePlayerRoomRequestDto {
    private Integer numberOfRounds = 5;
}
