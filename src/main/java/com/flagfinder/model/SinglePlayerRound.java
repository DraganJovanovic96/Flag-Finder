package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "single_player_rounds")
@EqualsAndHashCode(callSuper = false)
public class SinglePlayerRound extends BaseEntity {

    @OneToOne
    private Guess guess;

    @ManyToOne
    private SinglePlayerGame singlePlayerGame;

    @ManyToOne
    private Country country;

    @Column
    private Integer roundNumber;
}
