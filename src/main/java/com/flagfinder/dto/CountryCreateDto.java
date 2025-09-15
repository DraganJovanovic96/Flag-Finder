package com.flagfinder.dto;

import com.flagfinder.enumeration.Continent;
import lombok.Data;

import java.util.List;

/**
 * DTO for creating a new country.
 * Contains the necessary information to create a country entity.
 */
@Data
public class CountryCreateDto {
    /**
     * The name of the country.
     */
    private String nameOfCounty;
    
    /**
     * The URL of the country's flag image.
     */
    private String imageUrl;
    
    /**
     * List of continents or regions this country belongs to.
     */
    private List<Continent> continents;
}
