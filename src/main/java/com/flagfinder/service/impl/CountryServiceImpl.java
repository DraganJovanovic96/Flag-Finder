package com.flagfinder.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
import com.flagfinder.dto.RestCountryDto;
import com.flagfinder.enumeration.Continent;
import com.flagfinder.model.Country;
import com.flagfinder.repository.CountryRepository;
import com.flagfinder.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Country createCountryFromImageUrl(CountryCreateDto countryCreateDto) {
        try {
            Country country = new Country();
            country.setNameOfCounty(countryCreateDto.getNameOfCounty());
            country.setContinents(countryCreateDto.getContinents());

            if (countryCreateDto.getImageUrl() != null && !countryCreateDto.getImageUrl().isEmpty()) {
                try {
                    byte[] imageBytes = downloadImageFromUrl(countryCreateDto.getImageUrl());
                    country.setFlagImage(imageBytes);
                } catch (Exception e) {
                    log.error("Failed to download image from URL: {}", countryCreateDto.getImageUrl(), e);
                    throw new RuntimeException("Failed to download flag image", e);
                }
            }

            return countryRepository.save(country);
        } catch (Exception e) {
            log.error("Failed to create country", e);
            throw new RuntimeException("Failed to create country: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Override
    public Country getCountryById(UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with ID: " + id));
    }

    @Override
    public void deleteCountry(UUID id) {
        countryRepository.deleteById(id);
    }

    @Override
    public List<CountrySearchDto> searchCountriesByPrefix(String prefix, int limit) {
        try {
            if (prefix == null || prefix.trim().isEmpty()) {
                throw new RuntimeException("Search prefix cannot be empty");
            }

            List<Country> results = countryRepository.findByNameOfCountyStartingWithIgnoreCase(prefix.trim());

            // Limit results to specified number and convert to DTO
            return results.stream()
                    .limit(limit)
                    .map(country -> new CountrySearchDto(country.getId(), country.getNameOfCounty()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search countries with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to search countries: " + e.getMessage(), e);
        }
    }

    @Override
    public String loadCountriesFromRestApi() {
        try {
            String apiUrl = "https://restcountries.com/v3.1/all?fields=name,flags,continents";

            ResponseEntity<List<RestCountryDto>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RestCountryDto>>() {
                    }
            );

            List<RestCountryDto> restCountries = response.getBody();
            if (restCountries == null || restCountries.isEmpty()) {
                log.warn("No countries received from REST Countries API");
                return "No countries received from REST Countries API";
            }

            log.info("Received {} countries from API, processing...", restCountries.size());

            int savedCount = 0;
            for (RestCountryDto restCountry : restCountries) {
                try {
                    Country country = convertRestCountryToEntity(restCountry);
                    if (country != null) {
                        // Check if country already exists by name
                        List<Country> existingCountries = countryRepository.findByNameOfCountyStartingWithIgnoreCase(country.getNameOfCounty());
                        boolean exists = existingCountries.stream()
                                .anyMatch(existing -> existing.getNameOfCounty().equalsIgnoreCase(country.getNameOfCounty()));

                        if (!exists) {
                            countryRepository.save(country);
                            savedCount++;
                            log.debug("Saved country: {}", country.getNameOfCounty());
                        } else {
                            log.debug("Country already exists, skipping: {}", country.getNameOfCounty());
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to process country: {}", restCountry.getName() != null ? restCountry.getName().getCommon() : "unknown", e);
                }
            }

            String successMessage = "Successfully loaded " + savedCount + " countries from REST Countries API";
            log.info(successMessage);
            return successMessage;

        } catch (Exception e) {
            log.error("Failed to load countries from REST Countries API", e);
            throw new RuntimeException("Failed to load countries from API: " + e.getMessage(), e);
        }
    }

    @Override
    public String loadUsStatesFromRestApi() {
       try {
           AtomicInteger savedCount = new AtomicInteger();
           String url = "https://flagcdn.com/en/codes.json";

           // Parse JSON into Map<String, String>
           ObjectMapper mapper = new ObjectMapper();
           try (InputStream is = new URL(url).openStream()) {
               Map<String, String> countryCodes = mapper.readValue(is, Map.class);

               // Loop through and find US states
               countryCodes.entrySet().stream()
                       .filter(entry -> entry.getKey().startsWith("us-"))
                       .forEach(entry -> {
                           savedCount.getAndIncrement();
                           String code = entry.getKey();
                           String name = entry.getValue();
                           String flagUrl = "https://flagcdn.com/" + code + ".svg";
                           Country country = new Country();
                           List<Continent> continents = new ArrayList<>();
                           continents.add(Continent.USA_STATE);
                           country.setNameOfCounty(name);
                           byte[] flagImageBytes = null;
                           try {
                               flagImageBytes = downloadImageFromUrl(flagUrl);
                           } catch (IOException e) {
                               log.error("Failed to download flag image for {}: {}", country.getNameOfCounty(), flagUrl, e);
                           }
                           country.setFlagImage(flagImageBytes);
                           country.setContinents(continents);
                            countryRepository.save(country);
                       });
           }

           String successMessage = "Successfully loaded " + savedCount + " countries from FLAG CDN API";
           log.info(successMessage);
           return successMessage;
       }
       catch (Exception e) {
        log.error("Failed to load countries from REST US STATES API", e);
        throw new RuntimeException("Failed to load REST US STATES from API: " + e.getMessage(), e);
        }
    }

    private Country convertRestCountryToEntity(RestCountryDto restCountry) {
        if (restCountry.getName() == null || restCountry.getName().getCommon() == null) {
            log.warn("Skipping country with missing name data");
            return null;
        }
        
        Country country = new Country();
        country.setNameOfCounty(restCountry.getName().getCommon());
        
        // Convert continents
        List<Continent> continents = new ArrayList<>();
        if (restCountry.getContinents() != null) {
            for (String continentName : restCountry.getContinents()) {
                try {
                    Continent continent = mapStringToContinent(continentName);
                    if (continent != null) {
                        continents.add(continent);
                    }
                } catch (Exception e) {
                    log.warn("Unknown continent: {}", continentName);
                }
            }
        }
        country.setContinents(continents);
        
        // Download flag image
        if (restCountry.getFlags() != null && restCountry.getFlags().getSvg() != null) {
            try {
                byte[] flagImageBytes = downloadImageFromUrl(restCountry.getFlags().getSvg());
                country.setFlagImage(flagImageBytes);
            } catch (Exception e) {
                log.error("Failed to download flag image for {}: {}", country.getNameOfCounty(), restCountry.getFlags().getSvg(), e);
                // Continue without flag image
            }
        }
        
        return country;
    }
    
    private Continent mapStringToContinent(String continentName) {
        if (continentName == null) {
            return null;
        }
        
        return switch (continentName.toUpperCase()) {
            case "ASIA" -> Continent.ASIA;
            case "AFRICA" -> Continent.AFRICA;
            case "NORTH AMERICA" -> Continent.NORTH_AMERICA;
            case "SOUTH AMERICA" -> Continent.SOUTH_AMERICA;
            case "ANTARCTICA" -> Continent.ANTARCTICA;
            case "EUROPE" -> Continent.EUROPE;
            case "AUSTRALIA", "OCEANIA" -> Continent.AUSTRALIA;
            default -> {
                log.warn("Unknown continent name: {}", continentName);
                yield null;
            }
        };
    }
    
    /**
     * Downloads an image from URL and converts it to byte array
     * 
     * @param imageUrl the URL of the image
     * @return byte array of the image
     * @throws IOException if download fails
     */
    private byte[] downloadImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return outputStream.toByteArray();
        }
    }
    
    @Override
    public ResponseEntity<byte[]> getCountryFlagResponse(UUID id) {
        try {
            Country country = getCountryById(id);
            
            if (country.getFlagImage() == null) {
                log.warn("Country found but no flag image for: {}", country.getNameOfCounty());
                throw new RuntimeException("Flag image not found for country: " + country.getNameOfCounty());
            }
            
            log.debug("Serving flag image for: {}, size: {} bytes", country.getNameOfCounty(), country.getFlagImage().length);

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
            log.error("Failed to get flag for country ID: {}", id, e);
            throw new RuntimeException("Failed to get country flag: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Country> getCountriesByAnyContinents(List<com.flagfinder.enumeration.Continent> continents) {
        try {
            if (continents == null || continents.isEmpty()) {
                return getAllCountries();
            }
            
            List<String> continentNames = continents.stream()
                    .map(Enum::name)
                    .toList();
            
            return countryRepository.findByAnyContinentIn(continentNames);
        } catch (Exception e) {
            log.error("Failed to get countries by continents: {}", continents, e);
            throw new RuntimeException("Failed to get countries by continents: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Country getRandomCountryFromAnyContinents(List<com.flagfinder.enumeration.Continent> continents) {
        try {
            if (continents == null || continents.isEmpty()) {
                Country randomCountry = countryRepository.findRandomCountry();
                if (randomCountry == null) {
                    throw new RuntimeException("No countries found in database");
                }
                return randomCountry;
            }
            
            List<String> continentNames = continents.stream()
                    .map(Enum::name)
                    .toList();
            
            Country randomCountry = countryRepository.findRandomByAnyContinentIn(continentNames);
            if (randomCountry == null) {
                throw new RuntimeException("No countries found for continents: " + continents);
            }
            
            return randomCountry;
        } catch (Exception e) {
            log.error("Failed to get random country from continents: {}", continents, e);
            throw new RuntimeException("Failed to get random country from continents: " + e.getMessage(), e);
        }
    }
}
