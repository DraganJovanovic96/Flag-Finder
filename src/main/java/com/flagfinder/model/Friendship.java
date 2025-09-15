package com.flagfinder.model;

import com.flagfinder.enumeration.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity representing a friendship relationship between two users.
 * Tracks the status of friend requests and established friendships.
 */
@Entity
@Table(name = "friendship")
@Data
@EqualsAndHashCode(callSuper = false)
public class Friendship extends BaseEntity{

    /**
     * The user who initiated the friend request.
     */
    @ManyToOne
    private User initiator;

    /**
     * The user who received the friend request.
     */
    @ManyToOne
    private User target;

    /**
     * The current status of the friendship (PENDING, ACCEPTED, DECLINE).
     */
    @Enumerated(EnumType.STRING)
    private FriendshipStatus friendshipStatus;
}
