package com.flagfinder.controller;

import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
import com.flagfinder.model.Country;
import com.flagfinder.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

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
        try {
            Country createdCountry = countryService.createCountryFromImageUrl(countryCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCountry);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Gets all countries
     * 
     * @return ResponseEntity with list of countries
     */
    @GetMapping
    public ResponseEntity<List<Country>> getAllCountries() {
        List<Country> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
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
        if (country != null) {
            return ResponseEntity.ok(country);
        }
        return ResponseEntity.notFound().build();
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
        try {
            Country country = countryService.getCountryById(id);
            if (country == null) {
                System.out.println("Country not found for ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            if (country.getFlagImage() == null) {
                System.out.println("Country found but no flag image for: " + country.getNameOfCounty());
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Serving flag image for: " + country.getNameOfCounty() + ", size: " + country.getFlagImage().length + " bytes");

            String contentType = "image/png";
            byte[] imageData = country.getFlagImage();
            if (imageData.length > 4) {
                String header = new String(imageData, 0, Math.min(100, imageData.length));
                if (header.contains("<svg") || header.contains("<?xml")) {
                    contentType = "image/svg+xml";
                }
                else if (imageData[0] == (byte)0xFF && imageData[1] == (byte)0xD8) {
                    contentType = "image/jpeg";
                }
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "max-age=3600")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET")
                    .header("Access-Control-Allow-Headers", "*")
                    .body(country.getFlagImage());
        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Searches countries by name prefix
     * 
     * @param prefix the prefix to search for
     * @return ResponseEntity with list of matching countries (max 5)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CountrySearchDto>> searchCountries(@RequestParam String prefix) {
        System.out.println("Search endpoint called with prefix: " + prefix);
        
        if (prefix == null || prefix.trim().isEmpty()) {
            System.out.println("Empty prefix, returning bad request");
            return ResponseEntity.badRequest().build();
        }
        
        List<CountrySearchDto> countries = countryService.searchCountriesByPrefix(prefix, 5);
        System.out.println("Found " + countries.size() + " countries for prefix: " + prefix);
        
        return ResponseEntity.ok(countries);
    }

    /**
     * Deletes a country by ID
     * 
     * @param id the country ID
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable UUID id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}
