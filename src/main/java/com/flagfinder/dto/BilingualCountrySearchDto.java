package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for bilingual country search results.
 * Contains country information in both English and Serbian languages.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BilingualCountrySearchDto {
    /**
     * The unique identifier of the country.
     */
    private UUID id;
    
    /**
     * The English name of the country.
     */
    private String englishName;
    
    /**
     * The Serbian name of the country.
     */
    private String serbianName;
    
    /**
     * Returns a formatted display string with both names.
     * Format: "English Name (Serbian Name)" or just "English Name" if Serbian is null.
     *
     * @return formatted display name combining both languages
     */
    public String getDisplayName() {
        if (serbianName != null && !serbianName.isEmpty()) {
            return englishName + " (" + serbianName + ")";
        }
        return englishName;
    }
}
