package com.flagfinder.service.impl;

import com.flagfinder.dto.FriendRequestResponseDto;
import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.dto.SendFriendRequestDto;
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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final UserRepository userRepository;

    private final UserService userService;

    private final FriendshipRepository friendshipRepository;

    private final FriendshipMapper friendshipMapper;

    @Override
    public FriendshipDto sendFriendRequest(@Valid SendFriendRequestDto sendFriendRequestDto) {
            User target = userRepository
                    .findOneByUserNameIgnoreCase(sendFriendRequestDto.getUserName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist"));

              User initiator = userService.getUserFromAuthentication();

                if (target.equals(initiator)) {
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't send friend request to yourself.");
                 }

                Optional<Friendship> currentFriendship = friendshipRepository.findByInitiatorAndTarget(initiator, target);

                if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.PENDING) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friendship request already exists.");
                }

                if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.DENIED) {
                   friendshipRepository.delete(currentFriendship.get());
                 }

              if(currentFriendship.isPresent() && currentFriendship.get().getFriendshipStatus() == FriendshipStatus.ACCEPTED) {
                  throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are already friends.");
              }

                Friendship friendship = new Friendship();
                friendship.setInitiator(initiator);
                friendship.setTarget(target);
                friendship.setFriendshipStatus(FriendshipStatus.PENDING);

                //todo send a websocket friend request

                friendshipRepository.save(friendship);

                return friendshipMapper.friendshipToFriendshipDto(friendship);
        }

    @Override
    public FriendshipDto respondToFriendRequest(FriendRequestResponseDto friendRequestResponseDto) {

        User target = userService.getUserFromAuthentication();

        User initiator = userRepository.findOneByUserNameIgnoreCase(friendRequestResponseDto.getInitiatorUserName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist."));

        Friendship friendship = friendshipRepository.findByInitiatorAndTarget(initiator, target)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request doesn't exist."));


        if(friendship.getFriendshipStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"This friend request has already been answered.");
        }

        friendship.setFriendshipStatus(friendRequestResponseDto.getFriendshipStatus());
        friendshipRepository.save(friendship);

        return friendshipMapper.friendshipToFriendshipDto(friendship);
    }

    @Override
    public Page<FriendshipDto> findAllFriendShipRequests(Integer page, Integer pageSize) {

       User user = userService.getUserFromAuthentication();
       Page<Friendship> resultPage = friendshipRepository.findByTargetAndFriendshipStatus(user, FriendshipStatus.PENDING, PageRequest.of(page, pageSize));
       List<Friendship> friendships = resultPage.getContent();

       List<FriendshipDto> friendshipDtos = friendshipMapper.friendshipsToFriendshipDtos(friendships);
       return new PageImpl<>(friendshipDtos, resultPage.getPageable(), resultPage.getTotalElements());
    }
}
