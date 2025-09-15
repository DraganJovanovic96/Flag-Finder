package com.flagfinder.mapper;

import com.flagfinder.dto.RoundDto;
import com.flagfinder.dto.SinglePlayerRoundDto;
import com.flagfinder.model.Round;
import com.flagfinder.model.SinglePlayerRound;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between SinglePlayerRound entities and DTOs.
 * Handles mapping of single player round data with custom field mappings for country information and guess.
 * Uses GuessMapper for mapping nested guess objects.
 */
@Mapper(uses = {GuessMapper.class})
public interface SinglePlayerRoundMapper {

    /**
     * Maps a SinglePlayerRound entity to a SinglePlayerRoundDto.
     * Maps country information and guess to corresponding DTO fields.
     * Time remaining and round active status are ignored as they are calculated at runtime.
     *
     * @param singlePlayerRound the SinglePlayerRound entity to be mapped
     * @return a SinglePlayerRoundDto containing the single player round information
     */
    @Mapping(target = "countryName", source = "country.nameOfCounty")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "flagImage", source = "country.flagImage")
    @Mapping(target = "timeRemaining", ignore = true)
    @Mapping(target = "roundActive", ignore = true)
    @Mapping(target = "guess", source = "guess")
    SinglePlayerRoundDto singlePlayerRoundToSinglePlayerRoundDto(SinglePlayerRound singlePlayerRound);
}
