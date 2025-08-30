package com.flagfinder.mapper;

import com.flagfinder.dto.GameDto;
import com.flagfinder.model.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface GameMapper {

    /**
     * Maps a Game object to a GameDto object.
     *
     * @param game the Game object to be mapped to a GameDto object
     * @return a GameDto object containing the game's information
     */
    @Mapping(target = "hostName", expression = "java(game.getUsers().size() > 0 ? game.getUsers().get(0).getGameName() : null)")
    @Mapping(target = "guestName", expression = "java(game.getUsers().size() > 1 ? game.getUsers().get(1).getGameName() : null)")
    @Mapping(target = "playerNames", expression = "java(game.getUsers().stream().map(user -> user.getGameName()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "currentRound", ignore = true)
    @Mapping(target = "currentRoundData", ignore = true)
    GameDto gameToGameDto(Game game);
}
