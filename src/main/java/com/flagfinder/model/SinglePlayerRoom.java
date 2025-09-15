package com.flagfinder.model;

import com.flagfinder.enumeration.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name = "single_player_rooms")
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"host", "singlePlayerGame"}, callSuper = false)
public class SinglePlayerRoom extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status;
    
    @Column(name = "number_of_rounds")
    private Integer numberOfRounds = 5;
    
    @OneToOne(mappedBy = "singlePlayerRoom")
    private SinglePlayerGame singlePlayerGame;
}
