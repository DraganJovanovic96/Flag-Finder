package com.flagfinder.service.impl;

import com.flagfinder.dto.*;
import com.flagfinder.enumeration.RoomStatus;
import com.flagfinder.mapper.RoomMapper;
import com.flagfinder.model.Room;
import com.flagfinder.model.User;
import com.flagfinder.repository.RoomRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private static final String USER_NOT_PRESENT = "User doesn't exist.";
    private final ExtractAuthenticatedUserService extractAuthenticatedUserService;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public RoomDto createRoom() {
        User host = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        Optional<Room> roomAsHost = roomRepository.findOneByHost(host);
        Optional<Room> roomAsGuest = roomRepository.findOneByGuest(host);

        roomAsHost.ifPresent(roomRepository::delete);

        if (roomAsGuest.isPresent()) {
            Room guestRoom = roomAsGuest.get();
            guestRoom.setGuest(null);
            roomRepository.save(guestRoom);
        }

        Room room = new Room();
        room.setHost(host);
        room.setStatus(RoomStatus.WAITING_FOR_GUEST);
        roomRepository.save(room);

        return roomMapper.roomToRoomDtoMapper(room);
    }
    
    @Override
    public RoomDto joinRoom(JoinRoomRequestDto joinRoomRequestDto) {
        Room room = roomRepository.findOneById(joinRoomRequestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Room doesn't exist"));

        if (Objects.nonNull(room.getGuest())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is full");
        }

        User guest = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        if (room.getHost().equals(guest)) {
            throw new IllegalArgumentException("Cannot join your own room");
        }

        room.setGuest(guest);
        room.setStatus(RoomStatus.ROOM_READY_FOR_START);
        roomRepository.save(room);

        try {
            var roomDto = roomMapper.roomToRoomDtoMapper(room);
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    "/queue/room-updates",
                    roomDto
            );
        } catch (Exception e) {
            log.warn("Failed to send room update to host {}: {}", room.getHost().getGameName(), e.getMessage());
        }
        return roomMapper.roomToRoomDtoMapper(room);
    }
    
    @Override
    public InviteSentDto inviteFriend(InviteFriendRequestDto inviteFriendRequestDto) {
        System.out.println("this has been hit");

        User user = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        Room room = roomRepository.findOneByHost(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room doesn't exist."));

        if (!userRepository.existsByGameNameIgnoreCase(inviteFriendRequestDto.getFriendUserName())) {
            throw new IllegalArgumentException("Friend doesn't exist " + inviteFriendRequestDto.getFriendUserName());
        }

        if (room.getStatus() != RoomStatus.WAITING_FOR_GUEST) {
            throw new IllegalStateException("Can't invite friend to this room currently");
        }

        if (Objects.nonNull(room.getGuest())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is full");
        }

        if (room.getHost().getGameName().equals(inviteFriendRequestDto.getFriendUserName())) {
            throw new IllegalArgumentException("Cannot join your own room");
        }

        String targetUserName = inviteFriendRequestDto.getFriendUserName();
        User targetUser = userRepository.findOneByGameNameIgnoreCase(targetUserName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend doesn't exist."));

        GameInviteDto payload = new GameInviteDto();
        payload.setGameId(room.getId());
        payload.setTargetUserName(targetUserName);
        payload.setInitiatorUserName(user.getGameName());

        messagingTemplate.convertAndSendToUser(
                targetUser.getGameName(),
                "/queue/invites",
                payload
        );

        InviteSentDto inviteSentDto = new InviteSentDto();
        inviteSentDto.setRoomId(room.getId());
        inviteSentDto.setGuestUserName(inviteFriendRequestDto.getFriendUserName());

        return inviteSentDto;
    }

    @Override
    public void leaveRoom() {
 
         User user = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not found"));

         Room room = roomRepository.findOneByUser(user)
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in any room"));

          if (room.getHost().equals(user)) {
            if (room.getGuest() != null) {
                try {
                    messagingTemplate.convertAndSendToUser(
                            room.getGuest().getGameName(),
                            "/queue/room-closed",
                            "HOST_LEFT"
                    );
                } catch (Exception e) {
                    log.warn("Failed to notify guest {} about room close: {}", room.getGuest().getGameName(), e.getMessage());
                }
            }
            roomRepository.delete(room);
            return;
        }

        room.setGuest(null);
        room.setStatus(RoomStatus.WAITING_FOR_GUEST);
        roomRepository.save(room);
        try {
            var roomDto = roomMapper.roomToRoomDtoMapper(room);
            messagingTemplate.convertAndSendToUser(
                    room.getHost().getGameName(),
                    "/queue/room-updates",
                    roomDto
            );
        } catch (Exception e) {
            log.warn("Failed to send room update to host after guest left: {}", e.getMessage());
        }
    }

    @Override
    public RoomDto getRoomById(UUID id) {
        Room room = roomRepository.findOneById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room doesn't exist"));
        return roomMapper.roomToRoomDtoMapper(room);
    }

}
