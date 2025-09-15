package com.flagfinder.mapper;

import com.flagfinder.dto.RoomDto;
import com.flagfinder.dto.SinglePlayerRoomDto;
import com.flagfinder.model.Room;
import com.flagfinder.model.SinglePlayerRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between SinglePlayerRoom entities and DTOs.
 * Handles mapping of single player room data with custom field mappings for host user name.
 */
@Mapper
public interface SinglePlayerRoomMapper {
    /**
     * Maps a SinglePlayerRoom entity to a SinglePlayerRoomDto.
     * Maps host user game name to corresponding DTO field.
     *
     * @param singlePlayerRoom the SinglePlayerRoom entity to be mapped
     * @return a SinglePlayerRoomDto containing the single player room information
     */
    @Mapping(target = "hostUserName", source = "singlePlayerRoom.host.gameName")
    SinglePlayerRoomDto singlePlayerRoomToSinglePlayerRoomDto(SinglePlayerRoom singlePlayerRoom);
}
