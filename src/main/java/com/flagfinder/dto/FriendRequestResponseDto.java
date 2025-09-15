package com.flagfinder.dto;

import com.flagfinder.enumeration.FriendshipStatus;
import lombok.Data;

/**
 * DTO for friend request response information.
 * Contains details about the user who initiated the request and the current status.
 */
@Data
public class FriendRequestResponseDto {
    /**
     * The username of the user who initiated the friend request.
     */
    private String initiatorUserName;

    /**
     * The current status of the friendship request.
     */
    private FriendshipStatus friendshipStatus;
}
