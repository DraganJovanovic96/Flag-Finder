package com.flagfinder.service;

import com.flagfinder.dto.FriendRequestResponseDto;
import com.flagfinder.dto.FriendshipDto;
import com.flagfinder.dto.SendFriendRequestDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface FriendshipService {

    FriendshipDto sendFriendRequest(@Valid SendFriendRequestDto friendRequestDto);

    FriendshipDto respondToFriendRequest(@Valid FriendRequestResponseDto friendRequestResponseDto);

    Page<FriendshipDto> findAllFriendShipRequests(Integer page, Integer pageSize);
}
