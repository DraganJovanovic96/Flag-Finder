package com.flagfinder.model;

import com.flagfinder.enumeration.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Entity representing a single player room where solo games are configured.
 * Contains room settings and game configuration for single player mode.
 */
@Data
@Entity
@Table(name = "single_player_rooms")
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"host", "singlePlayerGame"}, callSuper = false)
public class SinglePlayerRoom extends BaseEntity {
    /**
     * The user who created this single player room.
     */
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    /**
     * Current status of the room.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status;
    
    /**
     * Number of rounds to be played in the single player game.
     * Default value is 5 rounds.
     */
    @Column(name = "number_of_rounds")
    private Integer numberOfRounds = 5;
    
    /**
     * The single player game associated with this room.
     * Created when the game starts.
     */
    @OneToOne(mappedBy = "singlePlayerRoom")
    private SinglePlayerGame singlePlayerGame;
}
