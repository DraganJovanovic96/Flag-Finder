package com.flagfinder.repository;

import com.flagfinder.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    Optional<Country> findByNameOfCounty(String countryName);
    
    Optional<Country> findByNameOfCountyIgnoreCase(String countryName);

    Optional<Country> findOneById(UUID countryId);

    @Query(
            value = "SELECT c.* " +
                    "FROM countries c " +
                    "JOIN country_continents cc ON c.id = cc.country_id " +
                    "WHERE cc.continent = :continent " +
                    "ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true
    )
    Country findRandomByContinent(@Param("continent") String continent);

    @Query(
            value = "SELECT * FROM countries ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true
    )
    Country findRandomCountry();
}
