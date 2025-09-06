package com.flagfinder.mapper;

import com.flagfinder.dto.GuessDto;
import com.flagfinder.model.Guess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface GuessMapper {

    /**
     * Maps a Guess object to a GuessDto object.
     *
     * @param guess the Guess object to be mapped to a GuessDto object
     * @return a GuessDto object containing the guess information
     */
    @Mapping(target = "userGameName", source = "user.gameName")
    @Mapping(target = "guessedCountryName", source = "guessedCountry.nameOfCounty")
    @Mapping(target = "guessedCountryId", source = "guessedCountry.id")
    @Mapping(target = "correct", source = "correct")
    GuessDto guessToGuessDto(Guess guess);
}
