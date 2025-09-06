package com.flagfinder.model;

import com.flagfinder.enumeration.Continent;
import com.flagfinder.enumeration.GameStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "games")
@EqualsAndHashCode(callSuper = false)
public class Game extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_games",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    @OneToMany(mappedBy = "game")
    private List<Round> rounds = new ArrayList<>();
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "winner_user_name")
    private String winnerUserName;

    @Column(name = "total_rounds")
    private Integer totalRounds;
    
    @Column(name = "host_score")
    private Integer hostScore;
    
    @Column(name = "guest_score")
    private Integer guestScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus status;
    
    @ElementCollection(targetClass = Continent.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "game_continents", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();
}
