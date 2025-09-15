package com.flagfinder.model;

import com.flagfinder.enumeration.Continent;
import com.flagfinder.enumeration.GameStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a game in the FlagFinder application.
 * Contains game information including players, rounds, scores, and timing.
 */
@Data
@Entity
@Table(name = "games")
@EqualsAndHashCode(callSuper = false)
public class Game extends BaseEntity{

    /**
     * The room where this game is being played.
     */
    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    /**
     * List of users participating in this game.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_games",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    /**
     * List of rounds played in this game.
     */
    @OneToMany(mappedBy = "game")
    private List<Round> rounds = new ArrayList<>();
    
    /**
     * Timestamp when the game started.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    /**
     * Timestamp when the game ended.
     */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    /**
     * Username of the game winner.
     */
    @Column(name = "winner_user_name")
    private String winnerUserName;

    /**
     * Total number of rounds in the game.
     */
    @Column(name = "total_rounds")
    private Integer totalRounds;
    
    /**
     * Current score of the host player.
     */
    @Column(name = "host_score")
    private Integer hostScore;
    
    /**
     * Current score of the guest player.
     */
    @Column(name = "guest_score")
    private Integer guestScore;
    
    /**
     * Current status of the game (IN_PROGRESS, COMPLETED, CANCELLED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus status;
    
    /**
     * List of continents included in this game for country selection.
     */
    @ElementCollection(targetClass = Continent.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "game_continents", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();
}
