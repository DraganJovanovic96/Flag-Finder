package com.flagfinder.mapper;

import com.flagfinder.dto.SinglePlayerGameDto;
import com.flagfinder.mapper.RoundMapper;
import com.flagfinder.model.SinglePlayerGame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper interface for converting between SinglePlayerGame entities and DTOs.
 * Handles mapping of single player game data with custom field mappings for user information.
 * Uses RoundMapper for mapping nested round objects.
 */
@Mapper(uses = {RoundMapper.class})
public interface SinglePlayerGameMapper {
    /**
     * Maps a SinglePlayerGame entity to a SinglePlayerGameDto.
     * Maps room ID and user information to corresponding DTO fields.
     * Current round is ignored as it's calculated at runtime.
     *
     * @param singlePlayerGame the SinglePlayerGame entity to be mapped
     * @return a SinglePlayerGameDto containing the single player game information
     */
    @Mapping(target = "roomId", source = "singlePlayerRoom.id")
    @Mapping(target = "hostName", expression = "java(singlePlayerGame.getUser().getGameName())")
    @Mapping(target = "playerName", expression = "java(singlePlayerGame.getUser().getUsername())")
    @Mapping(target = "currentRound", ignore = true)
    SinglePlayerGameDto singlePlayerGameToSinglePlayerGameDto(SinglePlayerGame singlePlayerGame);

    /**
     * Maps a list of SinglePlayerGame entities to a list of SinglePlayerGameDtos.
     *
     * @param singlePlayerGames the list of SinglePlayerGame entities to be mapped
     * @return a list of SinglePlayerGameDtos containing the single player games information
     */
    List<SinglePlayerGameDto> singlePlayerGamesToSinglePlayerGameDtos(List<SinglePlayerGame> singlePlayerGames);
}
