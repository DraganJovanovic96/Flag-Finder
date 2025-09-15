package com.flagfinder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DTO for external REST country API responses.
 * Maps country data from external APIs with nested structures for flags and names.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountryDto {
    /**
     * Flag information including SVG, PNG URLs and alt text.
     */
    private FlagsDto flags;
    
    /**
     * Country name information including common and official names.
     */
    private NameDto name;
    
    /**
     * List of continents this country belongs to.
     */
    private List<String> continents;
    
    /**
     * ISO 3166-1 alpha-2 country code.
     */
    private String cca2;
    
    /**
     * DTO for flag information from external country API.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlagsDto {
        /**
         * URL to the SVG version of the flag.
         */
        private String svg;
        
        /**
         * URL to the PNG version of the flag.
         */
        private String png;
        
        /**
         * Alternative text description for the flag.
         */
        private String alt;
    }
    
    /**
     * DTO for country name information from external country API.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameDto {
        /**
         * Common name of the country.
         */
        private String common;
        
        /**
         * Official name of the country.
         */
        private String official;
    }
}
