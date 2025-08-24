package com.flagfinder.service;

import com.flagfinder.dto.InviteFriendRequestDto;
import com.flagfinder.dto.InviteSentDto;
import com.flagfinder.dto.JoinRoomRequestDto;
import com.flagfinder.dto.RoomDto;

public interface RoomService {
    
    /**
     * Creates a new room for the specified host
     */
    RoomDto createRoom();
    
    /**
     * Joins a room as a guest
     */
    RoomDto joinRoom(JoinRoomRequestDto request);
    
    /**
     * Invites a friend to join a room
     */
    InviteSentDto inviteFriend(InviteFriendRequestDto inviteFriendRequestDto);

    void leaveRoom();
} 