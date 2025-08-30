package com.flagfinder.service;

import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.model.Country;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for country operations.
 */
public interface CountryService {
    
    /**
     * Creates a new country with flag image downloaded from URL
     * 
     * @param countryCreateDto the country data including image URL
     * @return the created country
     */
    Country createCountryFromImageUrl(CountryCreateDto countryCreateDto);
    
    /**
     * Gets all countries
     * 
     * @return list of all countries
     */
    List<Country> getAllCountries();
    
    /**
     * Gets a country by ID
     * 
     * @param id the country ID
     * @return the country or null if not found
     */
    Country getCountryById(UUID id);
    
    /**
     * Deletes a country by ID
     * 
     * @param id the country ID
     */
    void deleteCountry(UUID id);
}
