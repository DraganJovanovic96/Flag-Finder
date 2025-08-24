package com.flagfinder.model;

import com.flagfinder.enumeration.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name = "rooms")
@RequiredArgsConstructor
public class Room extends BaseEntity {
    @OneToOne
    private User host;
    @OneToOne
    private User guest;
    @Enumerated(EnumType.STRING)
    @Column(name = "room_status")
    private RoomStatus status;
} 