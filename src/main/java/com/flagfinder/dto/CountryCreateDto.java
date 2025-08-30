package com.flagfinder.dto;

import com.flagfinder.enumeration.Continent;
import lombok.Data;

import java.util.List;

@Data
public class CountryCreateDto {
    private String nameOfCounty;
    private String imageUrl;
    private List<Continent> continents;
}
