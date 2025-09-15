package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for friend-related notifications sent via WebSocket.
 * Contains information about friendship actions and status changes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendNotificationDto {
    /**
     * The username of the user who performed the action.
     */
    private String senderUsername;
    
    /**
     * The action that was performed (e.g., "sent", "accepted", "declined").
     */
    private String action;
    
    /**
     * The current status of the friendship.
     */
    private String friendshipStatus;
}
