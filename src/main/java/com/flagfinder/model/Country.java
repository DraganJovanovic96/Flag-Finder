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
public class Country extends  BaseEntity {

    @Column
    private String nameOfCounty;

    @Column
    private String serbianName;

    @Column(name = "country_code_alpha2")
    private String cca2;

    @Column(name = "flag_image", columnDefinition = "BYTEA")
    private byte[] flagImage;

    @ElementCollection(targetClass = Continent.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "country_continents", joinColumns = @JoinColumn(name = "country_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();

    @OneToMany(mappedBy = "guessedCountry")
    private List<Guess> guesses = new ArrayList<>();

    @OneToMany(mappedBy = "country")
    private List<Round> rounds = new ArrayList<>();
}
