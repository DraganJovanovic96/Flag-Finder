package com.flagfinder.mapper;

import com.flagfinder.dto.RoomDto;
import com.flagfinder.model.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface RoomMapper {

    @Mapping(target = "hostUserName", source = "room.host.gameName")
    @Mapping(target = "guestUserName", source = "room.guest.gameName")
    RoomDto roomToRoomDtoMapper(Room room);
}
