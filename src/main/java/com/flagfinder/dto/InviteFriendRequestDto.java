package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for sending a friend invitation request.
 * Contains the username of the user to be invited as a friend.
 */
@Data
public class InviteFriendRequestDto {
    /**
     * The username of the user to send a friend request to.
     */
    private String friendUserName;
}
