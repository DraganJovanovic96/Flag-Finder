package com.flagfinder.dto;

import com.flagfinder.enumeration.Continent;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GameStartRequestDto {
    private UUID roomId;
    private List<Continent> continents;
}
