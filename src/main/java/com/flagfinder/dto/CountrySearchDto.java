package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for country search results to avoid circular reference issues
 */
@Data
public class CountrySearchDto {
    private UUID id;
    private String nameOfCounty;
    
    public CountrySearchDto(UUID id, String nameOfCounty) {
        this.id = id;
        this.nameOfCounty = nameOfCounty;
    }
}
