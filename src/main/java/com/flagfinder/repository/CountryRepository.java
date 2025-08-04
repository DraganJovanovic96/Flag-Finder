package com.flagfinder.repository;

import com.flagfinder.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    Optional<Country> findByNameOfCounty(String countryName);

    Optional<Country> findOneById(UUID countryId);

}
