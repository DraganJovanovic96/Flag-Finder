package com.flagfinder.service.impl;

import com.flagfinder.dto.FriendNotificationDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the FriendshipService interface.
 * Provides comprehensive friendship management functionality including friend requests,
 * responses, friend list management, and real-time WebSocket notifications.
 * Handles bidirectional friendship relationships and online status tracking.
 */
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;

    private final UserService userService;

    private final FriendshipRepository friendshipRepository;

    private final FriendshipMapper friendshipMapper;

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Sends a friend request to another user.
     *
     * @param sendUserNameDto the DTO containing the username to send friend request to
     * @return a FriendshipDto representing the created friend request
     * @throws ResponseStatusException if user doesn't exist, trying to add self, or request already exists
     */
    @Override
    public FriendshipDto sendFriendRequest(@Valid SendUserNameDto sendUserNameDto) {
            User target = userRepository
                    .findOneByGameNameIgnoreCase(sendUserNameDto.getUserName())
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

                try {
                    FriendNotificationDto notification = new FriendNotificationDto(
                            initiator.getGameName(),
                            "REQUEST",
                            "PENDING"
                    );
                    messagingTemplate.convertAndSendToUser(
                            target.getGameName(),
                            "/queue/friend-request",
                            notification
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send friend request WebSocket notification: " + e.getMessage());
                }

                return friendshipMapper.friendshipToFriendshipDto(friendship);
        }

    /**
     * Responds to a friend request (accept or decline).
     *
     * @param friendRequestResponseDto the DTO containing the response to the friend request
     * @return a FriendshipDto representing the updated friendship
     * @throws ResponseStatusException if friend request is not found or user doesn't exist
     */
    @Override
    public FriendshipDto respondToFriendRequest(FriendRequestResponseDto friendRequestResponseDto) {

        User target = userService.getUserFromAuthentication();

        User initiator = userRepository.findOneByGameNameIgnoreCase(friendRequestResponseDto.getInitiatorUserName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist."));

        Friendship friendship = friendshipRepository.findByInitiatorAndTarget(initiator, target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request doesn't exist."));


        if(friendship.getFriendshipStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This friend request has already been answered.");
        }

        friendship.setFriendshipStatus(friendRequestResponseDto.getFriendshipStatus());
        friendshipRepository.save(friendship);

        try {
            String action = friendRequestResponseDto.getFriendshipStatus() == FriendshipStatus.ACCEPTED ? "ACCEPTED" : "DECLINED";
            FriendNotificationDto notification = new FriendNotificationDto(
                    target.getGameName(),
                    action,
                    friendRequestResponseDto.getFriendshipStatus().toString()
            );
            messagingTemplate.convertAndSendToUser(
                    initiator.getGameName(),
                    "/queue/friend-response",
                    notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send friend response WebSocket notification: " + e.getMessage());
        }

        return friendshipMapper.friendshipToFriendshipDto(friendship);
    }

    /**
     * Retrieves paginated friend requests for the authenticated user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return a Page of FriendshipDto objects representing pending friend requests
     */
    @Override
    public Page<FriendshipDto> findAllFriendShipRequests(Integer page, Integer pageSize) {

       User user = userService.getUserFromAuthentication();
       Page<Friendship> resultPage = friendshipRepository.findByTargetAndFriendshipStatus(user, FriendshipStatus.PENDING, PageRequest.of(page, pageSize));
       List<Friendship> friendships = resultPage.getContent();

       List<FriendshipDto> friendshipDtos = friendshipMapper.friendshipsToFriendshipDtos(friendships);
       return new PageImpl<>(friendshipDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }

    /**
     * Retrieves paginated friends list for the authenticated user.
     *
     * @param page the page number (0-based)
     * @param pageSize the number of items per page
     * @return a Page of FriendshipDto objects representing accepted friendships
     */
    @Override
    public Page<FriendshipDto> findAllFriends(Integer page, Integer pageSize) {
        User user = userService.getUserFromAuthentication();
        Page<Friendship> resultPage = friendshipRepository.findAllFriendshipsOfUser(user.getId(), FriendshipStatus.ACCEPTED, PageRequest.of(page, pageSize));
        List<Friendship> friendships = resultPage.getContent();

        List<FriendshipDto> friendshipDtos = friendshipMapper.friendshipsToFriendshipDtos(friendships);
        
        for (FriendshipDto dto : friendshipDtos) {
            String friendUsername = dto.getInitiatorUserName().equals(user.getGameName()) 
                ? dto.getTargetUserName() 
                : dto.getInitiatorUserName();
            
            Optional<User> friendUser = userRepository.findOneByGameNameIgnoreCase(friendUsername);
            if (friendUser.isPresent() && friendUser.get().getIsOnline() != null) {
                dto.setOnline(friendUser.get().getIsOnline());
            } else {
                dto.setOnline(false);
            }
        }
        
        return new PageImpl<>(friendshipDtos, PageRequest.of(page, pageSize), friendships.size());
    }

    /**
     * Removes a friend from the authenticated user's friends list.
     *
     * @param friendUsername the username of the friend to remove
     * @throws ResponseStatusException if user is not found or friendship doesn't exist
     */
    @Override
    public void removeFriend(String friendUsername) {
        User currentUser = userService.getUserFromAuthentication();
        
        User friendUser = userRepository.findOneByGameNameIgnoreCase(friendUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetweenUsers(currentUser, friendUser, FriendshipStatus.ACCEPTED);

        if (friendship.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship does not exist or is not accepted");
        }

        Friendship friendshipToDelete = friendship.get();
        friendshipRepository.delete(friendshipToDelete);

        try {
            FriendNotificationDto notification = new FriendNotificationDto(
                    currentUser.getGameName(),
                    "REMOVED",
                    "REMOVED"
            );
            messagingTemplate.convertAndSendToUser(
                    friendUser.getGameName(),
                    "/queue/friend-removed",
                    notification
            );
        } catch (Exception e) {
            System.err.println("Failed to send friend removal WebSocket notification: " + e.getMessage());
        }
    }
}
