package com.flagfinder.mapper;

import com.flagfinder.dto.RoomDto;
import com.flagfinder.dto.SinglePlayerRoomDto;
import com.flagfinder.model.Room;
import com.flagfinder.model.SinglePlayerRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SinglePlayerRoomMapper {
    @Mapping(target = "hostUserName", source = "singlePlayerRoom.host.gameName")
    SinglePlayerRoomDto singlePlayerRoomToSinglePlayerRoomDto(SinglePlayerRoom singlePlayerRoom);
}
