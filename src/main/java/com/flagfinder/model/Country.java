package com.flagfinder.model;

import com.flagfinder.enumeration.Continent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a country in the FlagFinder application.
 * Contains country information including names, flag data, and geographical classification.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "countries")
public class Country extends  BaseEntity {

    /**
     * The official name of the country.
     */
    @Column
    private String nameOfCounty;

    /**
     * The Serbian translation of the country name.
     */
    @Column
    private String serbianName;

    /**
     * The two-letter country code (ISO 3166-1 alpha-2).
     */
    @Column(name = "country_code_alpha2")
    private String cca2;

    /**
     * The flag image data stored as binary data.
     */
    @Column(name = "flag_image", columnDefinition = "BYTEA")
    private byte[] flagImage;

    /**
     * List of continents this country belongs to.
     * Some countries may belong to multiple continents.
     */
    @ElementCollection(targetClass = Continent.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "country_continents", joinColumns = @JoinColumn(name = "country_id"))
    @Column(name = "continent")
    private List<Continent> continents = new ArrayList<>();

    /**
     * List of guesses made for this country by players.
     */
    @OneToMany(mappedBy = "guessedCountry")
    private List<Guess> guesses = new ArrayList<>();

    /**
     * List of game rounds where this country was the target.
     */
    @OneToMany(mappedBy = "country")
    private List<Round> rounds = new ArrayList<>();
}
