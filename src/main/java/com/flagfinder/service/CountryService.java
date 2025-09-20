package com.flagfinder.service;

import com.flagfinder.dto.BilingualCountrySearchDto;
import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
import com.flagfinder.model.Country;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for country operations.
 * Provides methods for country management, search functionality, and integration with external APIs.
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
     * Deletes a country by its name.
     * 
     * @param countryName the name of the country to delete
     * @throws RuntimeException if country not found or deletion fails
     */
    public void deleteCountryByName(String countryName);

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
     * Gets a random country from any of the specified continents.
     * If continents list is null or empty, returns a random country from all continents.
     *
     * @param continents list of continents to filter by, or null for all continents
     * @return random country from any of the specified continents
     * @throws RuntimeException if no country found or filtering fails
     */
    Country getRandomCountryFromAnyContinents(List<com.flagfinder.enumeration.Continent> continents);
    
    /**
     * Gets a random country from any of the specified continents, excluding already used countries.
     * Ensures no duplicate countries appear in the same game.
     *
     * @param continents list of continents to filter by, or null for all continents
     * @param excludedCountryIds list of country IDs to exclude from selection
     * @return random country from specified continents not in the excluded list
     * @throws RuntimeException if no country found or filtering fails
     */
    Country getRandomCountryFromAnyContinentsExcluding(List<com.flagfinder.enumeration.Continent> continents, 
                                                      List<UUID> excludedCountryIds);
    
    /**
     * Gets a country by ID
     * 
     * @param id the country ID
     * @return the country
     * @throws RuntimeException if country not found
     */
    Country getCountryById(UUID id);
    
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
     * Searches countries by name prefix in both English and Serbian with text normalization
     * 
     * @param prefix the prefix to search for
     * @param limit maximum number of results to return
     * @return list of countries matching the prefix in either language
     * @throws RuntimeException if search fails
     */
    List<BilingualCountrySearchDto> searchCountriesBilingualByPrefix(String prefix, int limit);
    
    /**
     * Loads countries from REST Countries API and saves them to database
     * 
     * @return success message
     * @throws RuntimeException if loading fails
     */
    String loadCountriesFromRestApi();

    /**
     * Loads countries from REST Countries API and saves them to database
     *
     * @return success message
     * @throws RuntimeException if loading fails
     */
    String loadUsStatesFromRestApi();
    
    /**
     * Gets a country's flag image with content type detection
     * 
     * @param id the country ID
     * @return flag image response with headers
     * @throws RuntimeException if country or flag not found
     */
    org.springframework.http.ResponseEntity<byte[]> getCountryFlagResponse(UUID id);
}
