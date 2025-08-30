package com.flagfinder.controller;

import com.flagfinder.dto.InviteFriendRequestDto;
import com.flagfinder.dto.InviteSentDto;
import com.flagfinder.dto.JoinRoomRequestDto;
import com.flagfinder.dto.RoomDto;
import com.flagfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Slf4j
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
    public ResponseEntity<RoomDto> cancelRoom(
            @RequestParam(value = "token", required = false) String tokenParam,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        roomService.leaveRoom();
        return ResponseEntity.noContent().build();
    }

    /**
     * Get room by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoom(@PathVariable("id") java.util.UUID id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
}
