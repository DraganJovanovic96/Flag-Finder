package com.flagfinder.service;

import com.flagfinder.dto.*;

import java.util.UUID;

public interface RoomService {
    
    /**
     * Creates a new room for the specified host
     */
    RoomDto createRoom();

    /**
     * Creates a new room for the specified host
     */
    SinglePlayerRoomDto createSinglePlayerRoom();
    
    /**
     * Joins a room as a guest
     */
    RoomDto joinRoom(JoinRoomRequestDto request);
    
    /**
     * Invites a friend to join a room
     */
    InviteSentDto inviteFriend(InviteFriendRequestDto inviteFriendRequestDto);

    void leaveRoom();

    /**
     * Retrieves room details by id
     */
    RoomDto getRoomById(UUID id);
}
