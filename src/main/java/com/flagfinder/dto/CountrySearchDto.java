package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for country search results to avoid circular reference issues.
 * Provides a lightweight representation of country data for search operations.
 */
@Data
public class CountrySearchDto {
    /**
     * The unique identifier of the country.
     */
    private UUID id;
    
    /**
     * The name of the country.
     */
    private String nameOfCounty;
    
    /**
     * Constructor to create a CountrySearchDto with id and name.
     *
     * @param id the unique identifier of the country
     * @param nameOfCounty the name of the country
     */
    public CountrySearchDto(UUID id, String nameOfCounty) {
        this.id = id;
        this.nameOfCounty = nameOfCounty;
    }
}
