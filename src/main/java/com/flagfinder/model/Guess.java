package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "guesses")
@EqualsAndHashCode(callSuper = false)
public class Guess extends BaseEntity{

    @ManyToOne
    private Round round;

    @ManyToOne
    private User user;

    @ManyToOne
    private Country guessedCountry;

    @Column(name = "is_correct")
    private boolean correct;
}
