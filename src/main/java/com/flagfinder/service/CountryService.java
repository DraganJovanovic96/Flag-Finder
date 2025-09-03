package com.flagfinder.service;

import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
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
     * @throws RuntimeException if creation fails
     */
    Country createCountryFromImageUrl(CountryCreateDto countryCreateDto);
    
    /**
     * Gets all countries
     * 
     * @return list of all countries
     */
    List<Country> getAllCountries();
    
    /**
     * Gets countries filtered by continents (OR logic)
     * 
     * @param continents list of continents to filter by
     * @return list of countries from any of the specified continents
     * @throws RuntimeException if filtering fails
     */
    List<Country> getCountriesByAnyContinents(List<com.flagfinder.enumeration.Continent> continents);
    
    /**
     * Gets a random country from specified continents (OR logic)
     * 
     * @param continents list of continents to choose from
     * @return random country from any of the specified continents
     * @throws RuntimeException if no country found or filtering fails
     */
    Country getRandomCountryFromAnyContinents(List<com.flagfinder.enumeration.Continent> continents);
    
    /**
     * Gets a country by ID
     * 
     * @param id the country ID
     * @return the country
     * @throws RuntimeException if country not found
     */
    Country getCountryById(UUID id);
    
    /**
     * Deletes a country by ID
     * 
     * @param id the country ID
     */
    void deleteCountry(UUID id);
    
    /**
     * Searches countries by name prefix (case-insensitive)
     * 
     * @param prefix the prefix to search for
     * @param limit maximum number of results to return
     * @return list of countries matching the prefix
     * @throws RuntimeException if search fails
     */
    List<CountrySearchDto> searchCountriesByPrefix(String prefix, int limit);
    
    /**
     * Loads countries from REST Countries API and saves them to database
     * 
     * @return success message
     * @throws RuntimeException if loading fails
     */
    String loadCountriesFromRestApi();
    
    /**
     * Gets a country's flag image with content type detection
     * 
     * @param id the country ID
     * @return flag image response with headers
     * @throws RuntimeException if country or flag not found
     */
    org.springframework.http.ResponseEntity<byte[]> getCountryFlagResponse(UUID id);
}
