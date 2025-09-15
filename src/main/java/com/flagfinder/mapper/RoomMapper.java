package com.flagfinder.mapper;

import com.flagfinder.dto.RoomDto;
import com.flagfinder.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper interface for converting between Room entities and DTOs.
 * Handles mapping of room data with custom field mappings for host and guest user names.
 */
@Mapper
public interface RoomMapper {

    /**
     * Maps a Room entity to a RoomDto.
     * Maps host and guest user game names to corresponding DTO fields.
     *
     * @param room the Room entity to be mapped
     * @return a RoomDto containing the room information
     */
    @Mapping(target = "hostUserName", source = "room.host.gameName")
    @Mapping(target = "guestUserName", source = "room.guest.gameName")
    RoomDto roomToRoomDtoMapper(Room room);
}
