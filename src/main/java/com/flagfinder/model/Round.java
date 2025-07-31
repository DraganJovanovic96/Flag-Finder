package com.flagfinder.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "rounds")
@EqualsAndHashCode(callSuper = false)
public class Round extends BaseEntity {

    @OneToMany(mappedBy = "round")
    private List<Guess> guesses = new ArrayList<>();

    @ManyToOne
    private Game game;

    @ManyToOne
    private Country country;
}
