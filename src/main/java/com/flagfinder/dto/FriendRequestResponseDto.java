package com.flagfinder.dto;

import com.flagfinder.enumeration.FriendshipStatus;
import lombok.Data;

@Data
public class FriendRequestResponseDto {

    private String initiatorUserName;
    private String senderUsername;
    private boolean accepted;
    private FriendshipStatus friendshipStatus;
}
