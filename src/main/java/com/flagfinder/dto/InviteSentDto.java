package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for confirming that a game invitation has been sent.
 * Contains information about the invited user and the room.
 */
@Data
public class InviteSentDto {
    /**
     * The username of the user who was invited to the game.
     */
    private String guestUserName;

    /**
     * The unique identifier of the room for which the invitation was sent.
     */
    private UUID roomId;
}
