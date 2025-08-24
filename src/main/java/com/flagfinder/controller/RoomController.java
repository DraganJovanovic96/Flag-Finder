package com.flagfinder.controller;

import com.flagfinder.dto.InviteFriendRequestDto;
import com.flagfinder.dto.InviteSentDto;
import com.flagfinder.dto.JoinRoomRequestDto;
import com.flagfinder.dto.RoomDto;
import com.flagfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {
    
    private final RoomService roomService;
    
    /**
     * Creates a new room
     */
    @PostMapping("/create")
    public ResponseEntity<RoomDto> createRoom() {
        RoomDto roomDto = roomService.createRoom();
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDto);
    }

    
    /**
     * Joins a room as a guest
     */
    @PostMapping("/join")
    public ResponseEntity<RoomDto> joinRoom(@RequestBody JoinRoomRequestDto request) {
        RoomDto room = roomService.joinRoom(request);
        return ResponseEntity.ok(room);
    }
    
    /**
     * Invites a friend to a room
     */
    @PostMapping("/invite")
    public ResponseEntity<InviteSentDto> inviteFriend(@RequestBody InviteFriendRequestDto request) {
        return ResponseEntity.ok(roomService.inviteFriend(request));
    }

    
    /**
     * Cancels a room
     */
    @PostMapping("/cancel")
    public ResponseEntity<RoomDto> cancelRoom() {
        roomService.leaveRoom();
        return ResponseEntity.noContent().build();
    }
} 