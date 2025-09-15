package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for accepting a friend request.
 * Contains the ID of the user who initiated the friend request.
 */
@Data
public class AcceptFriendRequest {
   /**
    * The unique identifier of the user who sent the friend request.
    */
   private UUID initiatorId;
}
