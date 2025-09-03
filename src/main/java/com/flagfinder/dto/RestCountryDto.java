package com.flagfinder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountryDto {
    
    private FlagsDto flags;
    private NameDto name;
    private List<String> continents;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlagsDto {
        private String svg;
        private String png;
        private String alt;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameDto {
        private String common;
        private String official;
    }
}
