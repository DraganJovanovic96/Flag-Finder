package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity representing a player's guess in a game round.
 * Can be associated with either multiplayer or single player rounds.
 */
@Data
@Entity
@Table(name = "guesses")
@EqualsAndHashCode(callSuper = false)
public class Guess extends BaseEntity{

    /**
     * The multiplayer round this guess belongs to.
     * Null if this is a single player guess.
     */
    @ManyToOne
    private Round round;

    /**
     * The single player round this guess belongs to.
     * Null if this is a multiplayer guess.
     */
    @ManyToOne
    private SinglePlayerRound singlePlayerRound;

    /**
     * The user who made this guess.
     */
    @ManyToOne
    private User user;

    /**
     * The country that was guessed by the player.
     */
    @ManyToOne
    private Country guessedCountry;

    /**
     * Whether the guess was correct or not.
     */
    @Column(name = "is_correct")
    private boolean correct;
}
