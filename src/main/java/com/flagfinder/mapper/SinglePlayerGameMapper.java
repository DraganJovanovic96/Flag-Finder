package com.flagfinder.mapper;

import com.flagfinder.dto.SinglePlayerGameDto;
import com.flagfinder.mapper.RoundMapper;
import com.flagfinder.model.SinglePlayerGame;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {RoundMapper.class})
public interface SinglePlayerGameMapper {
    @Mapping(target = "roomId", source = "singlePlayerRoom.id")
    @Mapping(target = "hostName", expression = "java(singlePlayerGame.getUser().getGameName())")
    @Mapping(target = "playerName", expression = "java(singlePlayerGame.getUser().getUsername())")
    @Mapping(target = "currentRound", ignore = true)
    SinglePlayerGameDto singlePlayerGameToSinglePlayerGameDto(SinglePlayerGame singlePlayerGame);

    List<SinglePlayerGameDto> singlePlayerGamesToSinglePlayerGameDtos(List<SinglePlayerGame> singlePlayerGames);
}
