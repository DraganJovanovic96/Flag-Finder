package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a round in a multiplayer game.
 * Each round has a target country and collects guesses from players.
 */
@Data
@Entity
@Table(name = "rounds")
@EqualsAndHashCode(callSuper = false)
public class Round extends BaseEntity {

    /**
     * List of guesses made by players in this round.
     */
    @OneToMany(mappedBy = "round")
    private List<Guess> guesses = new ArrayList<>();

    /**
     * The game this round belongs to.
     */
    @ManyToOne
    private Game game;

    /**
     * The target country for this round that players need to guess.
     */
    @ManyToOne
    private Country country;

    /**
     * The sequential number of this round within the game (1, 2, 3, etc.).
     */
    @Column
    private Integer roundNumber;
}
