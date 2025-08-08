package com.flagfinder.model;

import com.flagfinder.enumeration.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "friendship")
@Data
@EqualsAndHashCode(callSuper = false)
public class Friendship extends BaseEntity{

    @ManyToOne
    private User initiator;

    @ManyToOne
    private User target;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus friendshipStatus;
}
