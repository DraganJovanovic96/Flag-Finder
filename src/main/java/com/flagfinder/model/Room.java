package com.flagfinder.model;

import com.flagfinder.enumeration.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "rooms")
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"host", "guest", "game"})
public class Room extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;
    
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private User guest;
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status;
    
    @OneToOne(mappedBy = "room")
    private Game game;
}
