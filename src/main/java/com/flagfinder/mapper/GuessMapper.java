package com.flagfinder.mapper;

import com.flagfinder.dto.GuessDto;
import com.flagfinder.model.Guess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between Guess entities and DTOs.
 * Handles mapping of guess data with custom field mappings for user and country information.
 */
@Mapper
public interface GuessMapper {

    /**
     * Maps a Guess entity to a GuessDto.
     * Maps user game name and guessed country information to corresponding DTO fields.
     *
     * @param guess the Guess entity to be mapped
     * @return a GuessDto containing the guess information
     */
    @Mapping(target = "userGameName", source = "user.gameName")
    @Mapping(target = "guessedCountryName", source = "guessedCountry.nameOfCounty")
    @Mapping(target = "guessedCountryId", source = "guessedCountry.id")
    @Mapping(target = "correct", source = "correct")
    GuessDto guessToGuessDto(Guess guess);
}
