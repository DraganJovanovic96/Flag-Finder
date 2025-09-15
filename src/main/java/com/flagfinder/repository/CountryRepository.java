package com.flagfinder.repository;

import com.flagfinder.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Country entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for country data access including search, random selection, and continent-based filtering.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, UUID> {

    /**
     * Finds a country by its exact name.
     *
     * @param countryName the exact name of the country
     * @return Optional containing the country if found, empty otherwise
     */
    Optional<Country> findByNameOfCounty(String countryName);
    
    /**
     * Finds a country by its name ignoring case sensitivity.
     *
     * @param countryName the name of the country (case insensitive)
     * @return Optional containing the country if found, empty otherwise
     */
    Optional<Country> findByNameOfCountyIgnoreCase(String countryName);

    /**
     * Finds a country by its unique identifier.
     *
     * @param countryId the UUID of the country
     * @return Optional containing the country if found, empty otherwise
     */
    Optional<Country> findOneById(UUID countryId);

    /**
     * Finds a random country from a specific continent.
     * Uses native SQL with RANDOM() for PostgreSQL compatibility.
     *
     * @param continent the continent name to filter by
     * @return a random Country from the specified continent
     */
    @Query(
            value = "SELECT c.* " +
                    "FROM countries c " +
                    "JOIN country_continents cc ON c.id = cc.country_id " +
                    "WHERE cc.continent = :continent " +
                    "ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true
    )
    Country findRandomByContinent(@Param("continent") String continent);

    /**
     * Finds a completely random country from all available countries.
     * Uses native SQL with RANDOM() for PostgreSQL compatibility.
     *
     * @return a random Country from the database
     */
    @Query(
            value = "SELECT * FROM countries ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true
    )
    Country findRandomCountry();

    /**
     * Finds countries whose names contain the given keyword (case insensitive).
     * Results are ordered alphabetically by country name.
     *
     * @param keyword the search keyword to match against country names
     * @return list of countries matching the keyword
     */
    @Query("SELECT c FROM Country c WHERE LOWER(c.nameOfCounty) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.nameOfCounty")
    List<Country> findByNameOfCountyContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Finds countries whose English or Serbian names contain the given keyword (case insensitive).
     * Searches both nameOfCounty and serbianName fields.
     *
     * @param keyword the search keyword to match against country names
     * @return list of countries matching the keyword in either language
     */
    @Query("SELECT c FROM Country c WHERE " +
           "LOWER(c.nameOfCounty) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.serbianName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.nameOfCounty")
    List<Country> findByNameOrSerbianNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Finds countries by normalized names (removing Serbian diacritics) for fuzzy search.
     * Normalizes both country names and search keyword by replacing Serbian special characters
     * with their Latin equivalents for better search matching.
     *
     * @param keyword the search keyword to normalize and match
     * @return list of countries matching the normalized keyword
     */
    @Query(value = "SELECT * FROM countries c WHERE " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
           "c.name_of_county, 'č', 'c'), 'ć', 'c'), 'đ', 'd'), 'š', 's'), 'ž', 'z'), 'Č', 'C'), 'Ć', 'C'), 'Đ', 'D'), 'Š', 'S'), 'Ž', 'Z'), 'dj', 'd')) " +
           "LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
           "CONCAT('%', :keyword, '%'), 'č', 'c'), 'ć', 'c'), 'đ', 'd'), 'š', 's'), 'ž', 'z'), 'Č', 'C'), 'Ć', 'C'), 'Đ', 'D'), 'Š', 'S'), 'Ž', 'Z'), 'dj', 'd')) OR " +
           "LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
           "c.serbian_name, 'č', 'c'), 'ć', 'c'), 'đ', 'd'), 'š', 's'), 'ž', 'z'), 'Č', 'C'), 'Ć', 'C'), 'Đ', 'D'), 'Š', 'S'), 'Ž', 'Z'), 'dj', 'd')) " +
           "LIKE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(" +
           "CONCAT('%', :keyword, '%'), 'č', 'c'), 'ć', 'c'), 'đ', 'd'), 'š', 's'), 'ž', 'z'), 'Č', 'C'), 'Ć', 'C'), 'Đ', 'D'), 'Š', 'S'), 'Ž', 'Z'), 'dj', 'd')) " +
           "ORDER BY c.name_of_county", 
           nativeQuery = true)
    List<Country> findByNormalizedNameOrSerbianNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * Finds all countries that belong to any of the specified continents.
     * Returns distinct countries to avoid duplicates from the many-to-many relationship.
     *
     * @param continents list of continent names to filter by
     * @return list of countries belonging to any of the specified continents
     */
    @Query(
            value = "SELECT DISTINCT c.* " +
                    "FROM countries c " +
                    "JOIN country_continents cc ON c.id = cc.country_id " +
                    "WHERE cc.continent IN :continents",
            nativeQuery = true
    )
    List<Country> findByAnyContinentIn(@Param("continents") List<String> continents);
    
    /**
     * Finds a random country from any of the specified continents.
     * Useful for game mechanics where countries should be selected from specific regions.
     *
     * @param continents list of continent names to choose from
     * @return a random Country from any of the specified continents
     */
    @Query(
            value = "SELECT c.* " +
                    "FROM countries c " +
                    "JOIN country_continents cc ON c.id = cc.country_id " +
                    "WHERE cc.continent IN :continents " +
                    "ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true
    )
    Country findRandomByAnyContinentIn(@Param("continents") List<String> continents);
}
