package com.flagfinder.service.impl;

import com.flagfinder.dto.InviteFriendRequestDto;
import com.flagfinder.dto.InviteSentDto;
import com.flagfinder.dto.JoinRoomRequestDto;
import com.flagfinder.dto.RoomDto;
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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private static final String USER_NOT_PRESENT = "User doesn't exist.";
    private final ExtractAuthenticatedUserService extractAuthenticatedUserService;

    
    @Override
    public RoomDto createRoom() {
        User host = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_PRESENT));

        if(roomRepository.findOneByHost(host).isPresent() || roomRepository.findOneByGuest(host).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is currently in a room");
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
        //TODO: Send websocket notif here so their screen is updated with guests info
        return roomMapper.roomToRoomDtoMapper(room);
    }
    
    @Override
    public InviteSentDto inviteFriend(InviteFriendRequestDto inviteFriendRequestDto) {
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
        
        // TODO: Send a WebSocket notification to the friend

        InviteSentDto inviteSentDto = new InviteSentDto();
        inviteSentDto.setRoomId(room.getId());
        inviteSentDto.setGuestUserName(inviteFriendRequestDto.getFriendUserName());

        return inviteSentDto;
    }

    //TODO leaves a room methods for guest and host, guest can leave the room and thats it if host leaves kill the room
    @Override
    public void leaveRoom() {

        User user = userRepository.findByEmail(extractAuthenticatedUserService.getAuthenticatedUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"User is not found"));

        Room room = roomRepository.findOneByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in any room"));

         if (room.getHost().equals(user)) {
            roomRepository.delete(room);

            return;
        }
        //TODO: Send weboscket noti to host
         room.setGuest(null);
         room.setStatus(RoomStatus.WAITING_FOR_GUEST);
         roomRepository.save(room);
    }

}