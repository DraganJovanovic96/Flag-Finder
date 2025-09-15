package com.flagfinder.mapper;

import com.flagfinder.dto.RoundDto;
import com.flagfinder.model.Round;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between Round entities and DTOs.
 * Handles mapping of round data with custom field mappings for country information and guesses.
 * Uses GuessMapper for mapping nested guess objects.
 */
@Mapper(uses = {GuessMapper.class})
public interface RoundMapper {

    /**
     * Maps a Round entity to a RoundDto.
     * Maps country information and guesses to corresponding DTO fields.
     * Time remaining and round active status are ignored as they are calculated at runtime.
     *
     * @param round the Round entity to be mapped
     * @return a RoundDto containing the round information
     */
    @Mapping(target = "countryName", source = "country.nameOfCounty")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "flagImage", source = "country.flagImage")
    @Mapping(target = "timeRemaining", ignore = true)
    @Mapping(target = "roundActive", ignore = true)
    @Mapping(target = "guesses", source = "guesses")
    RoundDto roundToRoundDto(Round round);
}
