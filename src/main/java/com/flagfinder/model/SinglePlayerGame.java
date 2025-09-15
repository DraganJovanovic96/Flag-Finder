package com.flagfinder.model;

import com.flagfinder.dto.SinglePlayerRoomDto;
import com.flagfinder.enumeration.Continent;
import com.flagfinder.enumeration.GameStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a single player game in the FlagFinder application.
 * Contains game information for solo play including rounds, score, and timing.
 */
@Data
@Entity
@Table(name = "single_player_games")
@EqualsAndHashCode(callSuper = false)
public class SinglePlayerGame extends BaseEntity{

    /**
     * The single player room where this game is being played.
     */
    @OneToOne
    @JoinColumn(name = "room_id")
    private SinglePlayerRoom singlePlayerRoom;

    /**
     * The user playing this single player game.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * List of rounds played in this single player game.
     */
    @OneToMany(mappedBy = "singlePlayerGame")
    private List<SinglePlayerRound> rounds = new ArrayList<>();
    
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
     * Total number of rounds in the game.
     */
    @Column(name = "total_rounds")
    private Integer totalRounds;
    
    /**
     * Current score of the player.
     */
    @Column(name = "host_score")
    private Integer hostScore;
    
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
    @CollectionTable(name = "single_player_game_continents", joinColumns = @JoinColumn(name = "single_player_game_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();
}
