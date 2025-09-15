package com.flagfinder.model;

import com.flagfinder.enumeration.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a game room where multiplayer games are hosted.
 * Contains information about host, guest, game settings, and room status.
 */
@Data
@Entity
@Table(name = "rooms")
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"host", "guest", "game"}, callSuper = false)
public class Room extends BaseEntity {
    /**
     * The user who created and hosts this room.
     */
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;
    
    /**
     * The user who joined this room as a guest.
     * Null if no guest has joined yet.
     */
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private User guest;
    
    /**
     * Current status of the room (WAITING_FOR_GUEST, ROOM_READY_FOR_START, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status;
    
    /**
     * Number of rounds to be played in the game.
     * Default value is 5 rounds.
     */
    @Column(name = "number_of_rounds")
    private Integer numberOfRounds = 5;
    
    /**
     * The game associated with this room.
     * Created when the game starts.
     */
    @OneToOne(mappedBy = "room")
    private Game game;
}
