package com.flagfinder.service.impl;

import com.flagfinder.dto.CountryCreateDto;
import com.flagfinder.model.Country;
import com.flagfinder.repository.CountryRepository;
import com.flagfinder.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {
    
    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public Country createCountryFromImageUrl(CountryCreateDto countryCreateDto) {
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
    }
    
    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
    
    @Override
    public Country getCountryById(UUID id) {
        return countryRepository.findById(id).orElse(null);
    }
    
    @Override
    public void deleteCountry(UUID id) {
        countryRepository.deleteById(id);
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
}
