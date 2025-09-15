package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a round in a single player game.
 * Each round has a target country and a single guess from the player.
 */
@Data
@Entity
@Table(name = "single_player_rounds")
@EqualsAndHashCode(callSuper = false)
public class SinglePlayerRound extends BaseEntity {

    /**
     * The guess made by the player in this round.
     */
    @OneToOne
    private Guess guess;

    /**
     * The single player game this round belongs to.
     */
    @ManyToOne
    private SinglePlayerGame singlePlayerGame;

    /**
     * The target country for this round that the player needs to guess.
     */
    @ManyToOne
    private Country country;

    /**
     * The sequential number of this round within the game (1, 2, 3, etc.).
     */
    @Column
    private Integer roundNumber;
}
