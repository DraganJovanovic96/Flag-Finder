package com.flagfinder.model;

import com.flagfinder.enumeration.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "friendship")
@Data
@EqualsAndHashCode(callSuper = false)
public class Friendship extends BaseEntity<Long>{

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;
}
