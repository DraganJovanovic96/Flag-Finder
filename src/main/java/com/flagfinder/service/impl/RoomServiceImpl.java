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

/**
 * Implementation of RoomService interface.
 * Manages room operations for both multiplayer and single player games.
 * Handles room creation, joining, leaving, invitations, and real-time WebSocket notifications.
 * Provides comprehensive room lifecycle management with status tracking and user validation.
 */
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


    /**
     * Creates a new multiplayer room for the authenticated user.
     *
     * @param request the DTO containing room creation parameters
     * @return a RoomDto object representing the created room
     * @throws ResponseStatusException if the user is not found
     */
    @Override
    public RoomDto createRoom(CreateRoomRequestDto request) {
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
        room.setNumberOfRounds(request.getNumberOfRounds() != null ? request.getNumberOfRounds() : 5);
        roomRepository.save(room);

        return roomMapper.roomToRoomDtoMapper(room);
    }

    /**
     * Creates a new single player room for the authenticated user.
     *
     * @param request the DTO containing single player room creation parameters
     * @return a SinglePlayerRoomDto object representing the created single player room
     * @throws ResponseStatusException if the user is not found
     */
    @Override
    public SinglePlayerRoomDto createSinglePlayerRoom(CreateSinglePlayerRoomRequestDto request) {
        User host = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        List<Room> existingHostRooms = roomRepository.findByHostAndStatusNotInActiveOrCompleted(host);
        roomRepository.deleteAll(existingHostRooms);

        SinglePlayerRoom singlePlayerRoom = new SinglePlayerRoom();
        singlePlayerRoom.setHost(host);
        singlePlayerRoom.setStatus(RoomStatus.WAITING_FOR_GUEST);
        singlePlayerRoom.setNumberOfRounds(request.getNumberOfRounds() != null ? request.getNumberOfRounds() : 5);
        singlePlayerRoomRepository.save(singlePlayerRoom);

        return singlePlayerRoomMapper.singlePlayerRoomToSinglePlayerRoomDto(singlePlayerRoom);
    }
    
    /**
     * Allows a user to join an existing room as a guest.
     *
     * @param joinRoomRequestDto the DTO containing the room ID to join
     * @return a RoomDto object representing the updated room with the new guest
     * @throws ResponseStatusException if the room doesn't exist, is full, or user is not found
     * @throws IllegalArgumentException if user tries to join their own room
     */
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
        }
        return roomMapper.roomToRoomDtoMapper(room);
    }
    
    /**
     * Sends a game invitation to a friend for the user's available room.
     *
     * @param inviteFriendRequestDto the DTO containing the friend's username to invite
     * @return an InviteSentDto object confirming the invitation was sent
     * @throws ResponseStatusException if user not found, no available room, or room is full
     * @throws IllegalArgumentException if friend doesn't exist or user tries to invite themselves
     */
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

    /**
     * Allows the authenticated user to leave their current active room.
     * If the user is the host and there's a guest, the room is closed and guest is notified.
     * If the user is a guest, they are removed from the room.
     *
     * @throws ResponseStatusException if user is not found or not in any active room
     */
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
                    RoomClosedDto roomClosedDto = new RoomClosedDto();
                    roomClosedDto.setRoomId(room.getId().toString());
                    roomClosedDto.setMessage("The room has been closed by the host.");
                    messagingTemplate.convertAndSendToUser(
                            room.getGuest().getGameName(),
                            "/queue/room-closed",
                            roomClosedDto
                    );
                } catch (Exception e) {
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
        }
    }

    /**
     * Retrieves a room by its unique identifier.
     *
     * @param id the unique UUID identifier of the room
     * @return a RoomDto object representing the room
     * @throws ResponseStatusException if the room doesn't exist
     */
    @Override
    public RoomDto getRoomById(UUID id) {
        Room room = roomRepository.findOneById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room doesn't exist"));
        return roomMapper.roomToRoomDtoMapper(room);
    }

    /**
     * Updates the number of rounds for a room. Only the host can update room settings.
     *
     * @param roomId the unique UUID identifier of the room to update
     * @param request the DTO containing the new number of rounds
     * @return a RoomDto object representing the updated room
     * @throws ResponseStatusException if user/room not found, user is not host, or room is in progress
     */
    @Override
    public RoomDto updateRounds(UUID roomId, UpdateRoundsRequestDto request) {
        User user = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        Room room = roomRepository.findOneById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room doesn't exist"));

        if (!room.getHost().equals(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the host can update room settings");
        }

        if (room.getStatus() != RoomStatus.WAITING_FOR_GUEST && room.getStatus() != RoomStatus.ROOM_READY_FOR_START) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update rounds for a room that is already in progress");
        }

        room.setNumberOfRounds(request.getNumberOfRounds());
        roomRepository.save(room);

        if (room.getGuest() != null) {
            try {
                var roomDto = roomMapper.roomToRoomDtoMapper(room);
                messagingTemplate.convertAndSendToUser(
                        room.getGuest().getGameName(),
                        "/queue/room-updates",
                        roomDto
                );
            } catch (Exception e) {
            }
        }

        return roomMapper.roomToRoomDtoMapper(room);
    }

}
