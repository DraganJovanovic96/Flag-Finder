package com.flagfinder.service;

import com.flagfinder.dto.*;

import java.util.UUID;

/**
 * Service interface for room operations.
 * Provides methods for creating, joining, managing multiplayer and single player rooms.
 */
public interface RoomService {
    
    /**
     * Creates a new multiplayer room for the specified host.
     *
     * @param request the room creation request containing room details
     * @return the created room DTO
     * @throws RuntimeException if room creation fails
     */
    RoomDto createRoom(CreateRoomRequestDto request);

    /**
     * Creates a new single player room for the specified host.
     *
     * @param request the single player room creation request
     * @return the created single player room DTO
     * @throws RuntimeException if room creation fails
     */
    SinglePlayerRoomDto createSinglePlayerRoom(CreateSinglePlayerRoomRequestDto request);
    
    /**
     * Joins an existing room as a guest player.
     *
     * @param request the join room request containing room and user details
     * @return the updated room DTO with guest information
     * @throws RuntimeException if room not found or join fails
     */
    RoomDto joinRoom(JoinRoomRequestDto request);
    
    /**
     * Invites a friend to join a room.
     *
     * @param inviteFriendRequestDto the invitation request containing friend and room details
     * @return confirmation DTO that invitation was sent
     * @throws RuntimeException if invitation fails
     */
    InviteSentDto inviteFriend(InviteFriendRequestDto inviteFriendRequestDto);

    /**
     * Leaves the current room for the authenticated user.
     *
     * @throws RuntimeException if user not in a room or leave fails
     */
    void leaveRoom();

    /**
     * Retrieves room details by its unique identifier.
     *
     * @param id the UUID of the room
     * @return the room DTO with current details
     * @throws RuntimeException if room not found
     */
    RoomDto getRoomById(UUID id);

    /**
     * Updates the number of rounds for a room.
     *
     * @param roomId the UUID of the room to update
     * @param request the request containing the new round count
     * @return the updated room DTO
     * @throws RuntimeException if room not found or update fails
     */
    RoomDto updateRounds(UUID roomId, UpdateRoundsRequestDto request);
}
