package com.flagfinder.mapper;

import com.flagfinder.dto.RoundDto;
import com.flagfinder.model.Round;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {GuessMapper.class})
public interface RoundMapper {

    /**
     * Maps a Round object to a RoundDto object.
     *
     * @param round the Round object to be mapped to a RoundDto object
     * @return a RoundDto object containing the round's information
     */
    @Mapping(target = "countryName", source = "country.nameOfCounty")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "flagImage", source = "country.flagImage")
    @Mapping(target = "timeRemaining", ignore = true)
    @Mapping(target = "roundActive", ignore = true)
    @Mapping(target = "guesses", source = "guesses")
    RoundDto roundToRoundDto(Round round);
}
