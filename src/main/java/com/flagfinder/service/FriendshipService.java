package com.flagfinder.service;

import com.flagfinder.dto.FriendRequestResponseDto;
import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.dto.SendUserNameDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

/**
 * Service interface for friendship operations.
 * Provides methods for managing friend requests, responses, and friendship relationships.
 */
public interface FriendshipService {

    /**
     * Sends a friend request to another user.
     *
     * @param friendRequestDto the DTO containing the target user's information
     * @return the created friendship DTO
     * @throws RuntimeException if user not found or request fails
     */
    FriendshipDto sendFriendRequest(@Valid SendUserNameDto friendRequestDto);

    /**
     * Responds to a friend request (accept or decline).
     *
     * @param friendRequestResponseDto the DTO containing the response information
     * @return the updated friendship DTO
     * @throws RuntimeException if friendship not found or response fails
     */
    FriendshipDto respondToFriendRequest(@Valid FriendRequestResponseDto friendRequestResponseDto);

    /**
     * Finds all pending friend requests for the current user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return paginated list of friendship requests
     */
    Page<FriendshipDto> findAllFriendShipRequests(Integer page, Integer pageSize);

    /**
     * Finds all accepted friends for the current user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return paginated list of friendships
     */
    Page<FriendshipDto> findAllFriends(Integer page, Integer pageSize);

    /**
     * Removes a friend relationship.
     *
     * @param friendUsername the username of the friend to remove
     * @throws RuntimeException if friendship not found or removal fails
     */
    void removeFriend(String friendUsername);
}
