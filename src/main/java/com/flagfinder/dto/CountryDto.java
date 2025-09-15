package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO representing a country.
 * Contains basic country information for transfer between layers.
 */
@Data
public class CountryDto {
    /**
     * The unique identifier of the country.
     */
    private UUID id;
    
    /**
     * The name of the country.
     */
    private String nameOfCounty;
}
