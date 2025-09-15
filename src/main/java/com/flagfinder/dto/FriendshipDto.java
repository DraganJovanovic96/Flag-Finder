package com.flagfinder.dto;

import com.flagfinder.enumeration.FriendshipStatus;
import lombok.Data;

@Data
public class FriendshipDto {
    private String targetUserName;
    private String initiatorUserName;
    private FriendshipStatus friendshipStatus;
    private boolean isOnline;
}
