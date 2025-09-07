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

@Data
@Entity
@Table(name = "single_player_games")
@EqualsAndHashCode(callSuper = false)
public class SinglePlayerGame extends BaseEntity{

    @OneToOne
    @JoinColumn(name = "room_id")
    private SinglePlayerRoom singlePlayerRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "singlePlayerGame")
    private List<SinglePlayerRound> rounds = new ArrayList<>();
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_rounds")
    private Integer totalRounds;
    
    @Column(name = "host_score")
    private Integer hostScore;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_status")
    private GameStatus status;
    
    @ElementCollection(targetClass = Continent.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "single_player_game_continents", joinColumns = @JoinColumn(name = "single_player_game_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();
}
