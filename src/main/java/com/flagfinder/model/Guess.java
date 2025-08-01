package com.flagfinder.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "guesses")
@EqualsAndHashCode(callSuper = false)
public class Guess extends BaseEntity<Long>{

    @ManyToOne
    private Round round;

    @ManyToOne
    private User user;

    @ManyToOne
    private Country country;
}
