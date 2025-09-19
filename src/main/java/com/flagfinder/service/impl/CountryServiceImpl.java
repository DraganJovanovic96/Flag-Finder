package com.flagfinder.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagfinder.dto.BilingualCountrySearchDto;
import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.dto.CountrySearchDto;
import com.flagfinder.dto.RestCountryDto;
import com.flagfinder.enumeration.Continent;
import com.flagfinder.model.Country;
import com.flagfinder.model.Round;
import com.flagfinder.model.SinglePlayerRound;
import com.flagfinder.repository.CountryRepository;
import com.flagfinder.repository.GuessRepository;
import com.flagfinder.repository.RoundRepository;
import com.flagfinder.repository.SinglePlayerRoundRepository;
import com.flagfinder.service.CountryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of CountryService interface.
 * Provides comprehensive country management functionality including creation, deletion, search,
 * external API integration, flag image handling, and bilingual search capabilities.
 * Supports both regular countries and US states with proper continent mapping.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GuessRepository guessRepository;
    private final RoundRepository roundRepository;
    private final SinglePlayerRoundRepository singlePlayerRoundRepository;

    /**
     * Creates a new country from the provided DTO with image URL.
     *
     * @param countryCreateDto the DTO containing country information and image URL
     * @return the created Country object
     * @throws RuntimeException if country creation or image download fails
     */
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
                    throw new RuntimeException("Failed to download flag image", e);
                }
            }

            return countryRepository.save(country);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create country: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a country by its name and cleans up all related data.
     *
     * @param countryName the name of the country to delete
     * @throws ResponseStatusException if the country is not found
     */
    @Override
    @Transactional
    public void deleteCountryByName(String countryName) {
        Country country = countryRepository.findByNameOfCountyIgnoreCase(countryName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Country not found"));

        List<SinglePlayerRound> singlePlayerRoundsWithCountry = singlePlayerRoundRepository.findByCountry(country);
        for (SinglePlayerRound singlePlayerRound : singlePlayerRoundsWithCountry) {
            singlePlayerRound.setGuess(null);
            singlePlayerRoundRepository.save(singlePlayerRound);
        }

        guessRepository.deleteByGuessedCountry(country);
        
        List<Round> roundsWithCountry = roundRepository.findByCountry(country);
        for (Round round : roundsWithCountry) {
            guessRepository.deleteByRound(round);
        }
        
        for (SinglePlayerRound singlePlayerRound : singlePlayerRoundsWithCountry) {
            guessRepository.deleteBySinglePlayerRound(singlePlayerRound);
        }

        roundRepository.deleteByCountry(country);
        singlePlayerRoundRepository.deleteByCountry(country);

        countryRepository.delete(country);
    }

    /**
     * Retrieves all countries from the database.
     *
     * @return a list of all Country objects
     */
    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    /**
     * Retrieves a country by its unique identifier.
     *
     * @param id the unique UUID identifier of the country
     * @return the Country object
     * @throws RuntimeException if the country is not found
     */
    @Override
    public Country getCountryById(UUID id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with ID: " + id));
    }

    /**
     * Searches for countries by name prefix with a specified limit.
     *
     * @param prefix the search prefix to match country names
     * @param limit the maximum number of results to return
     * @return a list of CountrySearchDto objects matching the prefix
     * @throws RuntimeException if the search fails or prefix is empty
     */
    @Override
    public List<CountrySearchDto> searchCountriesByPrefix(String prefix, int limit) {
        try {
            if (prefix == null || prefix.trim().isEmpty()) {
                throw new RuntimeException("Search prefix cannot be empty");
            }

            List<Country> results = countryRepository.findByNameOfCountyContainingIgnoreCase(prefix.trim());

            return results.stream()
                    .limit(limit)
                    .map(country -> new CountrySearchDto(country.getId(), country.getNameOfCounty()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search countries: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for countries by name prefix in both English and Serbian with a specified limit.
     *
     * @param prefix the search prefix to match country names in either language
     * @param limit the maximum number of results to return
     * @return a list of BilingualCountrySearchDto objects matching the prefix
     * @throws RuntimeException if the search fails or prefix is empty
     */
    @Override
    public List<BilingualCountrySearchDto> searchCountriesBilingualByPrefix(String prefix, int limit) {
        try {
            if (prefix == null || prefix.trim().isEmpty()) {
                throw new RuntimeException("Search prefix cannot be empty");
            }

            String trimmedPrefix = prefix.trim();
            java.util.Set<Country> uniqueResults = new java.util.LinkedHashSet<>();
            
            List<Country> originalResults = countryRepository.findByNameOrSerbianNameContainingIgnoreCase(trimmedPrefix);
            uniqueResults.addAll(originalResults);
            
            List<Country> normalizedResults = countryRepository.findByNormalizedNameOrSerbianNameContainingIgnoreCase(trimmedPrefix);
            uniqueResults.addAll(normalizedResults);

            return uniqueResults.stream()
                    .limit(limit)
                    .map(country -> new BilingualCountrySearchDto(
                            country.getId(), 
                            country.getNameOfCounty(), 
                            country.getSerbianName()
                    ))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to search countries bilingually: " + e.getMessage(), e);
        }
    }

    /**
     * Loads countries from the REST Countries API and saves them to the database.
     * Fetches country data including names, flags, continents, and country codes.
     * Skips countries that already exist in the database.
     *
     * @return success message with count of loaded countries
     * @throws RuntimeException if API call fails or data processing fails
     */
    @Override
    public String loadCountriesFromRestApi() {
        try {
            String apiUrl = "https://restcountries.com/v3.1/all?fields=name,flags,continents,cca2";

            ResponseEntity<List<RestCountryDto>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RestCountryDto>>() {
                    }
            );

            List<RestCountryDto> restCountries = response.getBody();
            if (restCountries == null || restCountries.isEmpty()) {
                return "No countries received from REST Countries API";
            }


            int savedCount = 0;
            for (RestCountryDto restCountry : restCountries) {
                try {
                    Country country = convertRestCountryToEntity(restCountry);
                    if (country != null) {
                        List<Country> existingCountries = countryRepository.findByNameOfCountyContainingIgnoreCase(country.getNameOfCounty());
                        boolean exists = existingCountries.stream()
                                .anyMatch(existing -> existing.getNameOfCounty().equalsIgnoreCase(country.getNameOfCounty()));

                        if (!exists) {
                            countryRepository.save(country);
                            savedCount++;
                        } else {
                        }
                    }
                } catch (Exception e) {
                }
            }

            String successMessage = "Successfully loaded " + savedCount + " countries from REST Countries API";
            return successMessage;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load countries from API: " + e.getMessage(), e);
        }
    }

    /**
     * Loads US states from the Flag CDN API and saves them to the database.
     * Fetches state data including names, flag images, and Serbian translations.
     * All states are categorized under the USA_STATE continent.
     *
     * @return success message with count of loaded states
     * @throws RuntimeException if API call fails or data processing fails
     */
    @Override
    public String loadUsStatesFromRestApi() {
       try {
           AtomicInteger savedCount = new AtomicInteger();
           String url = "https://flagcdn.com/en/codes.json";

           ObjectMapper mapper = new ObjectMapper();
           try (InputStream is = new URL(url).openStream()) {
               Map<String, String> countryCodes = mapper.readValue(is, Map.class);

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
                           
                           String stateCode = code.substring(3).toUpperCase();
                           country.setCca2(stateCode);
                           country.setSerbianName(translateStateToSerbianLatin(stateCode));
                           
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

    /**
     * Converts a REST API country DTO to a Country entity.
     * Maps country names, continents, flag images, and country codes.
     * Downloads flag images from external URLs.
     *
     * @param restCountry the REST API country DTO
     * @return the converted Country entity, or null if conversion fails
     */
    private Country convertRestCountryToEntity(RestCountryDto restCountry) {
        if (restCountry.getName() == null || restCountry.getName().getCommon() == null) {
            log.warn("Skipping country with missing name data");
            return null;
        }
        
        Country country = new Country();
        country.setNameOfCounty(restCountry.getName().getCommon());
        
        country.setCca2(restCountry.getCca2());
        country.setSerbianName(translateToSerbianLatin(restCountry.getCca2()));
        
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
        
        if (restCountry.getFlags() != null && restCountry.getFlags().getSvg() != null) {
            try {
                byte[] flagImageBytes = downloadImageFromUrl(restCountry.getFlags().getSvg());
                country.setFlagImage(flagImageBytes);
            } catch (Exception e) {
                log.error("Failed to download flag image for {}: {}", country.getNameOfCounty(), restCountry.getFlags().getSvg(), e);
            }
        }
        
        return country;
    }
    
    /**
     * Maps continent name strings to Continent enum values.
     * Handles various continent name formats and aliases.
     *
     * @param continentName the continent name string
     * @return the corresponding Continent enum, or null if not found
     */
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
     * Translates country ISO codes to Serbian Latin script names.
     * Uses Java's Locale system for automatic translation.
     *
     * @param isoCode the ISO country code
     * @return the Serbian Latin name, or null if translation fails
     */
    private String translateToSerbianLatin(String isoCode) {
        if (isoCode == null || isoCode.isEmpty()) {
            return null;
        }

        try {
            Locale countryLocale = new Locale("", isoCode.toUpperCase());
            Locale serbianLatin = new Locale.Builder().setLanguage("sr").setScript("Latn").build();
            return countryLocale.getDisplayCountry(serbianLatin);
        } catch (Exception e) {
            log.debug("Failed to translate country code {} to Serbian: {}", isoCode, e.getMessage());
            return null;
        }
    }

    /**
     * Translates US state codes to Serbian Latin script names.
     * Uses a comprehensive mapping of all US states and territories.
     *
     * @param stateCode the US state code (e.g., "CA", "NY")
     * @return the Serbian Latin name of the state, or null if not found
     */
    private String translateStateToSerbianLatin(String stateCode) {
        if (stateCode == null || stateCode.isEmpty()) {
            return null;
        }
        
        return switch (stateCode.toUpperCase()) {
            case "AL" -> "Alabama";
            case "AK" -> "Aljaska";
            case "AZ" -> "Arizona";
            case "AR" -> "Arkanzas";
            case "CA" -> "Kalifornija";
            case "CO" -> "Kolorado";
            case "CT" -> "Konektikut";
            case "DE" -> "Delaver";
            case "FL" -> "Florida";
            case "GA" -> "Džordžija";
            case "HI" -> "Havaji";
            case "ID" -> "Ajdaho";
            case "IL" -> "Ilinois";
            case "IN" -> "Indijana";
            case "IA" -> "Ajova";
            case "KS" -> "Kanzas";
            case "KY" -> "Kentaki";
            case "LA" -> "Lujzijana";
            case "ME" -> "Mejn";
            case "MD" -> "Merilend";
            case "MA" -> "Masačusets";
            case "MI" -> "Mičigen";
            case "MN" -> "Minesota";
            case "MS" -> "Misisipi";
            case "MO" -> "Misuri";
            case "MT" -> "Montana";
            case "NE" -> "Nebraska";
            case "NV" -> "Nevada";
            case "NH" -> "Nju Hempšir";
            case "NJ" -> "Nju Džerzi";
            case "NM" -> "Nju Meksiko";
            case "NY" -> "Njujork";
            case "NC" -> "Severna Karolina";
            case "ND" -> "Severna Dakota";
            case "OH" -> "Ohajo";
            case "OK" -> "Oklahoma";
            case "OR" -> "Oregon";
            case "PA" -> "Pensilvanija";
            case "RI" -> "Rod Ajlend";
            case "SC" -> "Južna Karolina";
            case "SD" -> "Južna Dakota";
            case "TN" -> "Tenesi";
            case "TX" -> "Teksas";
            case "UT" -> "Juta";
            case "VT" -> "Vermont";
            case "VA" -> "Virdžinija";
            case "WA" -> "Vašington";
            case "WV" -> "Zapadna Virdžinija";
            case "WI" -> "Viskonsin";
            case "WY" -> "Vajoming";
            case "DC" -> "Distrikt Kolumbija";
            default -> {
                log.debug("No Serbian translation found for state code: {}", stateCode);
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
    
    /**
     * Retrieves a country's flag image as an HTTP response.
     * Automatically detects image format (PNG, JPEG, SVG) and sets appropriate headers.
     * Enhances SVG flags with minimum dimensions for better visibility.
     *
     * @param id the UUID of the country
     * @return ResponseEntity containing the flag image with proper headers
     * @throws RuntimeException if country not found or flag image unavailable
     */
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
                    imageData = enhanceSvgWithMinimumSize(imageData);
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
    
    /**
     * Retrieves countries that belong to any of the specified continents.
     * If no continents are specified, returns all countries.
     *
     * @param continents list of continents to filter by
     * @return list of countries belonging to any of the specified continents
     * @throws RuntimeException if database query fails
     */
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
    
    /**
     * Gets a random country from any of the specified continents.
     * If continents list is null or empty, returns a random country from all continents.
     *
     * @param continents list of continents to filter by, or null for all continents
     * @return random country from any of the specified continents
     * @throws RuntimeException if no countries found or database query fails
     */
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
    
    /**
     * Gets a random country from any of the specified continents, excluding already used countries.
     * Ensures no duplicate countries appear in the same game.
     *
     * @param continents list of continents to filter by, or null for all continents
     * @param excludedCountryIds list of country IDs to exclude from selection
     * @return random country from specified continents not in the excluded list
     * @throws RuntimeException if no countries found or database query fails
     */
    @Override
    public Country getRandomCountryFromAnyContinentsExcluding(List<com.flagfinder.enumeration.Continent> continents, 
                                                            List<UUID> excludedCountryIds) {
        try {
            if (excludedCountryIds == null || excludedCountryIds.isEmpty()) {
                return getRandomCountryFromAnyContinents(continents);
            }
            
            if (continents == null || continents.isEmpty()) {
                Country randomCountry = countryRepository.findRandomCountryExcluding(excludedCountryIds);
                if (randomCountry == null) {
                    throw new RuntimeException("No countries found excluding already used countries");
                }
                return randomCountry;
            }
            
            List<String> continentNames = continents.stream()
                    .map(Enum::name)
                    .toList();
            
            Country randomCountry = countryRepository.findRandomByAnyContinentInExcluding(continentNames, excludedCountryIds);
            if (randomCountry == null) {
                throw new RuntimeException("No countries found for continents: " + continents + " excluding already used countries");
            }
            
            return randomCountry;
        } catch (Exception e) {
            log.error("Failed to get random country from continents: {} excluding: {}", continents, excludedCountryIds, e);
            throw new RuntimeException("Failed to get random country from continents excluding used countries: " + e.getMessage(), e);
        }
    }
    
    /**
     * Enhances SVG flags with minimum dimensions for better visibility
     */
    private byte[] enhanceSvgWithMinimumSize(byte[] originalSvgData) {
        try {
            String svgContent = new String(originalSvgData, StandardCharsets.UTF_8);
            
            int minWidth = 200;
            int minHeight = 133;
            
            Pattern widthPattern = Pattern.compile("width=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            Pattern heightPattern = Pattern.compile("height=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
            
            Matcher widthMatcher = widthPattern.matcher(svgContent);
            Matcher heightMatcher = heightPattern.matcher(svgContent);
            
            boolean hasWidth = widthMatcher.find();
            boolean hasHeight = heightMatcher.find();
            
            int currentWidth = minWidth;
            int currentHeight = minHeight;
            
            if (hasWidth) {
                try {
                    String widthStr = widthMatcher.group(1).replaceAll("[^0-9.]", "");
                    currentWidth = (int) Double.parseDouble(widthStr);
                } catch (NumberFormatException e) {
                    log.debug("Could not parse width from SVG, using minimum");
                }
            }
            
            if (hasHeight) {
                try {
                    String heightStr = heightMatcher.group(1).replaceAll("[^0-9.]", "");
                    currentHeight = (int) Double.parseDouble(heightStr);
                } catch (NumberFormatException e) {
                    log.debug("Could not parse height from SVG, using minimum");
                }
            }
            
            if (currentWidth < minWidth || currentHeight < minHeight) {
                double scaleX = (double) minWidth / currentWidth;
                double scaleY = (double) minHeight / currentHeight;
                double scale = Math.max(scaleX, scaleY);
                
                int newWidth = (int) (currentWidth * scale);
                int newHeight = (int) (currentHeight * scale);
                
                if (hasWidth) {
                    svgContent = widthMatcher.replaceFirst("width=\"" + newWidth + "\"");
                } else {
                    svgContent = svgContent.replaceFirst("<svg", "<svg width=\"" + newWidth + "\"");
                }
                
                heightMatcher = heightPattern.matcher(svgContent);
                if (heightMatcher.find()) {
                    svgContent = heightMatcher.replaceFirst("height=\"" + newHeight + "\"");
                } else {
                    svgContent = svgContent.replaceFirst("<svg", "<svg height=\"" + newHeight + "\"");
                }
                
                if (!svgContent.contains("viewBox")) {
                    svgContent = svgContent.replaceFirst("<svg", 
                        "<svg viewBox=\"0 0 " + newWidth + " " + newHeight + "\"");
                }
                
                log.debug("Enhanced SVG dimensions from {}x{} to {}x{}", 
                    currentWidth, currentHeight, newWidth, newHeight);
            }
            
            return svgContent.getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.warn("Failed to enhance SVG with minimum size, returning original: {}", e.getMessage());
            return originalSvgData;
        }
    }
}
