package com.flagfinder.mapper;

import com.flagfinder.dto.CompletedGameDto;
import com.flagfinder.dto.GameDto;
import com.flagfinder.model.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {RoundMapper.class})
public interface GameMapper {
    /**
     * Maps a Game object to a GameDto object.
     *
     * @param game the Game object to be mapped to a GameDto object
     * @return a GameDto object containing the game's information
     */
    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "hostName", expression = "java(game.getUsers().size() > 0 ? game.getUsers().get(0).getGameName() : null)")
    @Mapping(target = "guestName", expression = "java(game.getUsers().size() > 1 ? game.getUsers().get(1).getGameName() : null)")
    @Mapping(target = "playerNames", expression = "java(game.getUsers().stream().map(user -> user.getGameName()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "currentRound", ignore = true)
    @Mapping(target = "currentRoundData", ignore = true)
    GameDto gameToGameDto(Game game);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "hostUserName", expression = "java(game.getUsers().size() > 0 ? game.getUsers().get(0).getGameName() : null)")
    @Mapping(target = "guestUserName", expression = "java(game.getUsers().size() > 1 ? game.getUsers().get(1).getGameName() : null)")
    @Mapping(target = "winnerUserName", source = "winnerUserName")
    @Mapping(target = "hostScore", source = "hostScore")
    @Mapping(target = "guestScore", source = "guestScore")
    @Mapping(target = "roundDtos", source = "rounds")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "endedAt", source = "endedAt")
    CompletedGameDto gameToCompletedGameDto(Game game);

    List<GameDto> gamesToGameDtos(List<Game> games);
}
