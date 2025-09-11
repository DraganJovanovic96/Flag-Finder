package com.flagfinder.mapper;

import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.model.Friendship;
import com.flagfinder.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper
public interface FriendshipMapper {
    /**
     * Maps a Friendship object to a FriendshipDto object.
     * Note: This mapping assumes the initiator is the friend.
     * The service layer should handle determining which user is the friend.
     *
     * @param friendship the Friendship object to be mapped to a FriendshipDto object
     * @return a FriendshipDto object containing the friendship's information
     */

    @Mapping(source = "initiator.gameName", target = "username")
    @Mapping(source = "createdAt", target = "createdAt")
    FriendshipDto friendshipToFriendshipDto(Friendship friendship);

    List<FriendshipDto> friendshipsToFriendshipDtos (List <Friendship> friendships);
    
    /**
     * Creates a FriendshipDto from a friend User and creation date
     */
    default FriendshipDto createFriendshipDto(User friend, java.time.LocalDateTime createdAt) {
        return new FriendshipDto(friend.getGameName(), createdAt);
    }
    
    /**
     * Maps Instant to LocalDateTime for MapStruct
     */
    default LocalDateTime map(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
