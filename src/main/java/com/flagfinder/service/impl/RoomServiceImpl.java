package com.flagfinder.service.impl;

import com.flagfinder.dto.*;
import com.flagfinder.enumeration.RoomStatus;
import com.flagfinder.mapper.RoomMapper;
import com.flagfinder.mapper.SinglePlayerRoomMapper;
import com.flagfinder.model.Room;
import com.flagfinder.model.SinglePlayerRoom;
import com.flagfinder.model.User;
import com.flagfinder.repository.RoomRepository;
import com.flagfinder.repository.SinglePlayerRoomRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final SinglePlayerRoomRepository singlePlayerRoomRepository;
    private final RoomMapper roomMapper;
    private final SinglePlayerRoomMapper singlePlayerRoomMapper;
    private static final String USER_NOT_PRESENT = "User doesn't exist.";
    private final ExtractAuthenticatedUserService extractAuthenticatedUserService;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public RoomDto createRoom() {
        User host = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        List<Room> existingHostRooms = roomRepository.findByHostAndStatusNotInActiveOrCompleted(host);
        roomRepository.deleteAll(existingHostRooms);

        List<Room> existingGuestRooms = roomRepository.findByGuestAndStatusNotInActiveOrCompleted(host);
        for (Room existingRoom : existingGuestRooms) {
            existingRoom.setGuest(null);
            roomRepository.save(existingRoom);
        }

        Room room = new Room();
        room.setHost(host);
        room.setStatus(RoomStatus.WAITING_FOR_GUEST);
        roomRepository.save(room);

        return roomMapper.roomToRoomDtoMapper(room);
    }

    @Override
    public SinglePlayerRoomDto createSinglePlayerRoom() {
        User host = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        List<Room> existingHostRooms = roomRepository.findByHostAndStatusNotInActiveOrCompleted(host);
        roomRepository.deleteAll(existingHostRooms);

        SinglePlayerRoom singlePlayerRoom = new SinglePlayerRoom();
        singlePlayerRoom.setHost(host);
        singlePlayerRoom.setStatus(RoomStatus.WAITING_FOR_GUEST);
        singlePlayerRoomRepository.save(singlePlayerRoom);

        return singlePlayerRoomMapper.singlePlayerRoomToSinglePlayerRoomDto(singlePlayerRoom);
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
        User user = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        List<Room> hostRooms = roomRepository.findByHost(user);
        Room room = hostRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.WAITING_FOR_GUEST)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No available room to invite friend to."));

        if (!userRepository.existsByGameNameIgnoreCase(inviteFriendRequestDto.getFriendUserName())) {
            throw new IllegalArgumentException("Friend doesn't exist " + inviteFriendRequestDto.getFriendUserName());
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

         List<Room> hostRooms = roomRepository.findByHost(user);
         List<Room> guestRooms = roomRepository.findByGuest(user);
         
         Room room = null;
         
         room = hostRooms.stream()
                 .filter(r -> r.getStatus() == RoomStatus.WAITING_FOR_GUEST || r.getStatus() == RoomStatus.ROOM_READY_FOR_START)
                 .findFirst()
                 .orElse(null);
         
         if (room == null) {
             room = guestRooms.stream()
                     .filter(r -> r.getStatus() == RoomStatus.WAITING_FOR_GUEST || r.getStatus() == RoomStatus.ROOM_READY_FOR_START)
                     .findFirst()
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in any active room"));
         }

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
