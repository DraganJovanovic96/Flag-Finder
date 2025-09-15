package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for game invitation information.
 * Contains details about the users involved in the game invitation and the game ID.
 */
@Data
public class GameInviteDto {
    /**
     * The username of the user who initiated the game invitation.
     */
    private String initiatorUserName;

    /**
     * The username of the user who is being invited to the game.
     */
    private String targetUserName;

    /**
     * The unique identifier of the game for which the invitation is sent.
     */
    private UUID gameId;
}
