package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CountryDto {
    private UUID id;
    private String nameOfCounty;
}
