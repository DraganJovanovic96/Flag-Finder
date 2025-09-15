package com.flagfinder.mapper;

import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.model.Friendship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper interface for converting between Friendship entities and DTOs.
 * Handles mapping of friendship data with custom field mappings for user names.
 */
@Mapper
public interface FriendshipMapper {
    /**
     * Maps a Friendship entity to a FriendshipDto.
     * Maps initiator and target user game names to corresponding DTO fields.
     *
     * @param friendship the Friendship entity to be mapped
     * @return a FriendshipDto containing the friendship information
     */
    @Mapping(source = "initiator.gameName", target = "initiatorUserName")
    @Mapping(source = "target.gameName", target = "targetUserName")
    FriendshipDto friendshipToFriendshipDto(Friendship friendship);

    /**
     * Maps a list of Friendship entities to a list of FriendshipDtos.
     *
     * @param friendships the list of Friendship entities to be mapped
     * @return a list of FriendshipDtos containing the friendships information
     */
    List<FriendshipDto> friendshipsToFriendshipDtos(List<Friendship> friendships);
}
