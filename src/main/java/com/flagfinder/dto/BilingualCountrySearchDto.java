package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BilingualCountrySearchDto {
    private UUID id;
    private String englishName;
    private String serbianName;
    
    /**
     * Returns a formatted display string with both names
     * Format: "English Name (Serbian Name)" or just "English Name" if Serbian is null
     */
    public String getDisplayName() {
        if (serbianName != null && !serbianName.isEmpty()) {
            return englishName + " (" + serbianName + ")";
        }
        return englishName;
    }
}
