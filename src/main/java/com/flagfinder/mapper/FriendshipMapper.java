package com.flagfinder.mapper;

import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.model.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface FriendshipMapper {
    /**
     * Maps a Friendship object to a FriendshipDto object.
     *
     * @param friendship the Friendship object to be mapped to a FriendshipDto object
     * @return a FriendshipDto object containing the friendship's information
     */

    @Mapping(source = "initiator.gameName", target = "initiatorUserName")
    @Mapping(source = "target.gameName", target = "targetUserName")
    FriendshipDto friendshipToFriendshipDto(Friendship friendship);

    List<FriendshipDto> friendshipsToFriendshipDtos (List <Friendship> friendships);
}
