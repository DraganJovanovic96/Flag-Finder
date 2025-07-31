package com.flagfinder.model;

import com.flagfinder.enumeration.Continent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "countries")
public class Country extends BaseEntity {

    @Column
    private String nameOfCounty;

    @Column
    private List<Continent> continents = new ArrayList<>();

    @Lob
    @Column(name = "image", columnDefinition = "BYTEA")
    private byte[] flagImage;

    @OneToMany(mappedBy = "country")
    private List<Guess> guesses = new ArrayList<>();

    @OneToMany(mappedBy = "country")
    private List<Round> rounds = new ArrayList<>();

}
