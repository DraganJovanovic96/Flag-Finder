package com.flagfinder.mapper;

import com.flagfinder.dto.RoundDto;
import com.flagfinder.dto.SinglePlayerRoundDto;
import com.flagfinder.model.Round;
import com.flagfinder.model.SinglePlayerRound;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {GuessMapper.class})
public interface SinglePlayerRoundMapper {

    /**
     * Maps a SinglePlayerRound object to a SinglePlayerRoundDto object.
     *
     * @param singlePlayerRound the SinglePlayerRoundD object to be mapped to a SinglePlayerRoundDto object
     * @return a SinglePlayerRoundDto object containing the singlePlayerRound's information
     */
    @Mapping(target = "countryName", source = "country.nameOfCounty")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "flagImage", source = "country.flagImage")
    @Mapping(target = "timeRemaining", ignore = true)
    @Mapping(target = "roundActive", ignore = true)
    @Mapping(target = "guess", source = "guess")
    SinglePlayerRoundDto singlePlayerRoundToSinglePlayerRoundDto(SinglePlayerRound singlePlayerRound);
}
