package com.flagfinder.service.impl;

import com.flagfinder.dto.FriendRequestResponseDto;
import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.dto.SendUserNameDto;
import com.flagfinder.enumeration.FriendshipStatus;
import com.flagfinder.mapper.FriendshipMapper;
import com.flagfinder.model.Friendship;
import com.flagfinder.model.User;
import com.flagfinder.repository.FriendshipRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.FriendshipService;
import com.flagfinder.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final FriendshipRepository friendshipRepository;
    private final FriendshipMapper friendshipMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    private static final String QUEUE_FRIEND_REQUEST = "/queue/friend-request";
    private static final String QUEUE_FRIEND_RESPONSE = "/queue/friend-response";

    @Override
    public FriendshipDto sendFriendRequest(@Valid SendUserNameDto sendUserNameDto) {
            String targetUsername = sendUserNameDto.getTargetUsername() != null ? 
                sendUserNameDto.getTargetUsername() : sendUserNameDto.getUserName();
            
            User target = userRepository
                    .findOneByGameNameIgnoreCase(targetUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist"));

              User initiator = userService.getUserFromAuthentication();

                if (target.equals(initiator)) {
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't send friend request to yourself.");
                 }

                Optional<Friendship> currentFriendship = friendshipRepository.findByInitiatorAndTarget(initiator, target);

                if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.PENDING) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friendship request already exists.");
                }

                if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.DECLINE) {
                   friendshipRepository.delete(currentFriendship.get());
                 }

              if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already friends.");
              }

                Friendship friendship = new Friendship();
                friendship.setInitiator(initiator);
                friendship.setTarget(target);
                friendship.setFriendshipStatus(FriendshipStatus.PENDING);

                friendshipRepository.save(friendship);
                
                FriendshipDto friendshipDto = friendshipMapper.friendshipToFriendshipDto(friendship);
                
                log.info("Sending WebSocket friend request notification from {} to {}", 
                        initiator.getGameName(), target.getGameName());
                try {
                    java.util.Map<String, Object> notification = java.util.Map.of(
                        "senderUsername", initiator.getGameName(),
                        "message", sendUserNameDto.getMessage() != null ? sendUserNameDto.getMessage() : ""
                    );
                    
                    messagingTemplate.convertAndSendToUser(
                            target.getGameName(),
                            QUEUE_FRIEND_REQUEST,
                            notification
                    );
                    log.info("Successfully sent friend request notification to {}", target.getGameName());
                } catch (Exception e) {
                    log.error("Failed to send friend request WebSocket notification to {}: {}", 
                            target.getGameName(), e.getMessage());
                }

                return friendshipDto;
        }

    @Override
    public FriendshipDto respondToFriendRequest(FriendRequestResponseDto friendRequestResponseDto) {

        User target = userService.getUserFromAuthentication();

        String initiatorUsername = friendRequestResponseDto.getSenderUsername() != null ? 
            friendRequestResponseDto.getSenderUsername() : friendRequestResponseDto.getInitiatorUserName();
            
        User initiator = userRepository.findOneByGameNameIgnoreCase(initiatorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist."));

        Friendship friendship = friendshipRepository.findByInitiatorAndTarget(initiator, target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request doesn't exist."));


        if(friendship.getFriendshipStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This friend request has already been answered.");
        }

        FriendshipStatus status = friendRequestResponseDto.getFriendshipStatus();
        if (status == null) {
            status = friendRequestResponseDto.isAccepted() ? FriendshipStatus.ACCEPTED : FriendshipStatus.DECLINE;
        }
        
        friendship.setFriendshipStatus(status);
        friendshipRepository.save(friendship);
        
        FriendshipDto friendshipDto = friendshipMapper.friendshipToFriendshipDto(friendship);
        
        log.info("Sending WebSocket friend response notification from {} to {}", 
                target.getGameName(), initiator.getGameName());
        try {
            java.util.Map<String, Object> notification = java.util.Map.of(
                "username", target.getGameName(),
                "accepted", status == FriendshipStatus.ACCEPTED,
                "message", ""
            );
            
            messagingTemplate.convertAndSendToUser(
                    initiator.getGameName(),
                    QUEUE_FRIEND_RESPONSE,
                    notification
            );
            log.info("Successfully sent friend response notification to {}", initiator.getGameName());
        } catch (Exception e) {
            log.error("Failed to send friend response WebSocket notification to {}: {}", 
                    initiator.getGameName(), e.getMessage());
        }

        return friendshipDto;
    }

    @Override
    public Page<FriendshipDto> findAllFriendShipRequests(Integer page, Integer pageSize) {

       User user = userService.getUserFromAuthentication();
       Page<Friendship> resultPage = friendshipRepository.findByTargetAndFriendshipStatus(user, FriendshipStatus.PENDING, PageRequest.of(page, pageSize));
       List<Friendship> friendships = resultPage.getContent();

       List<FriendshipDto> friendshipDtos = friendshipMapper.friendshipsToFriendshipDtos(friendships);
       return new PageImpl<>(friendshipDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }

    @Override
    public Page<FriendshipDto> findAllFriends(Integer page, Integer pageSize) {
        User user = userService.getUserFromAuthentication();
        Page<Friendship> resultPage = friendshipRepository.findAllFriendshipsOfUser(user.getId(), FriendshipStatus.ACCEPTED, PageRequest.of(page, pageSize));
        List<Friendship> friendships = resultPage.getContent();

        List<FriendshipDto> friendshipDtos = friendships.stream()
                .map(friendship -> {
                    // Determine which user is the friend (not the current user)
                    User friend = friendship.getInitiator().equals(user) ? 
                            friendship.getTarget() : friendship.getInitiator();
                    return friendshipMapper.createFriendshipDto(friend, 
                            LocalDateTime.ofInstant(friendship.getCreatedAt(), ZoneId.systemDefault()));
                })
                .toList();
        
        return new PageImpl<>(friendshipDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }

    @Override
    public void removeFriend(String friendUsername) {
        User currentUser = userService.getUserFromAuthentication();
        
        User friend = userRepository.findOneByGameNameIgnoreCase(friendUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist"));

        Optional<Friendship> friendship1 = friendshipRepository.findByInitiatorAndTarget(currentUser, friend);
        Optional<Friendship> friendship2 = friendshipRepository.findByInitiatorAndTarget(friend, currentUser);
        if (friendship1.isPresent() && friendship1.get().getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(friendship1.get());
        } else if (friendship2.isPresent() && friendship2.get().getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
            friendshipRepository.delete(friendship2.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship doesn't exist or is not accepted");
        }
        
        log.info("Friendship removed between {} and {}", currentUser.getGameName(), friend.getGameName());
    }
}
