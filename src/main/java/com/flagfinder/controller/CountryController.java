package com.flagfinder.controller;

import com.flagfinder.dto.BilingualCountrySearchDto;
import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
import com.flagfinder.enumeration.Continent;
import com.flagfinder.model.Country;
import com.flagfinder.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Controller class for handling country-related API endpoints.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@CrossOrigin
public class CountryController {
    
    private final CountryService countryService;

    /**
     * Creates a new country with flag image from URL
     * 
     * @param countryCreateDto the country data including image URL
     * @return ResponseEntity with created country
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Country> createCountry(@RequestBody CountryCreateDto countryCreateDto) {
        Country createdCountry = countryService.createCountryFromImageUrl(countryCreateDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdCountry);
    }

    /**
     * Gets all countries
     * 
     * @return ResponseEntity with list of countries
     */
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries(
            @RequestParam(required = false) List<Continent> continents) {
        List<Country> countries;
        
        if (continents != null && !continents.isEmpty()) {
            countries = countryService.getCountriesByAnyContinents(continents);
        } else {
            countries = countryService.getAllCountries();
        }

        return ResponseEntity.ok(countries);
    }

    /**
     * Deletes a country by name (Admin only)
     *
     * @param countryName the name of the country to delete
     * @return a ResponseEntity object with status code 204 (No Content)
     * @throws ResponseStatusException if the country is not found
     */
    @DeleteMapping("/{countryName}")
    @PreAuthorize("hasAuthority('admin:delete')")
    public ResponseEntity<Void> deleteCountry(@PathVariable String countryName) {
        countryService.deleteCountryByName(countryName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets a country by ID
     * 
     * @param id the country ID
     * @return ResponseEntity with country
     */
    @GetMapping("/{id}")
    public ResponseEntity<Country> getCountryById(@PathVariable UUID id) {
        Country country = countryService.getCountryById(id);

        return ResponseEntity.ok(country);
    }

    /**
     * Gets a country's flag image
     * 
     * @param id the country ID
     * @return ResponseEntity with flag image as byte array
     */
    @GetMapping("/{id}/flag")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> getCountryFlag(@PathVariable UUID id) {
        return countryService.getCountryFlagResponse(id);
    }

    /**
     * Searches countries by name prefix
     * 
     * @param prefix the prefix to search for
     * @return ResponseEntity with list of matching countries (max 5)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CountrySearchDto>> searchCountries(@RequestParam String prefix) {
        List<CountrySearchDto> countries = countryService.searchCountriesByPrefix(prefix, 5);

        return ResponseEntity.ok(countries);
    }

    /**
     * Searches countries by name prefix in both English and Serbian with text normalization
     * 
     * @param prefix the prefix to search for
     * @return ResponseEntity with list of matching countries (max 10)
     */
    @GetMapping("/search/bilingual")
    public ResponseEntity<List<BilingualCountrySearchDto>> searchCountriesBilingual(@RequestParam String prefix) {
        List<BilingualCountrySearchDto> countries = countryService.searchCountriesBilingualByPrefix(prefix, 10);

        return ResponseEntity.ok(countries);
    }

    /**
     * Loads countries from REST Countries API (Admin only)
     * 
     * @return ResponseEntity with success message
     */
    @PostMapping("/load-countries-api")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<String> loadCountriesFromApi() {
        String result = countryService.loadCountriesFromRestApi();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/load-us-states-api")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<String> loadUsStatesApi() {
        String result = countryService.loadUsStatesFromRestApi();

        return ResponseEntity.ok(result);
    }
    
    /**
     * Gets a random country from specified continents
     * 
     * @param continents list of continents to choose from (optional)
     * @return ResponseEntity with random country
     */
    @GetMapping("/random")
    public ResponseEntity<Country> getRandomCountry(
            @RequestParam(required = false) List<Continent> continents) {
        Country randomCountry = countryService.getRandomCountryFromAnyContinents(continents);
        
        return ResponseEntity.ok(randomCountry);
    }
}
