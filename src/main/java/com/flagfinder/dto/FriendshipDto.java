package com.flagfinder.dto;

import com.flagfinder.enumeration.FriendshipStatus;
import lombok.Data;

/**
 * DTO representing a friendship between two users.
 * Contains information about both users and the friendship status.
 */
@Data
public class FriendshipDto {
    /**
     * The username of the target user in the friendship.
     */
    private String targetUserName;
    
    /**
     * The username of the user who initiated the friendship.
     */
    private String initiatorUserName;
    
    /**
     * The current status of the friendship.
     */
    private FriendshipStatus friendshipStatus;
    
    /**
     * Indicates whether the friend is currently online.
     */
    private boolean isOnline;
}
