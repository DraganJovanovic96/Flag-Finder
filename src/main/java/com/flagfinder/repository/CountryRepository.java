package com.flagfinder.repository;

import com.flagfinder.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT c FROM Country c WHERE LOWER(c.nameOfCounty) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY c.nameOfCounty")
    List<Country> findByNameOfCountyContainingIgnoreCase(@Param("keyword") String keyword);

    @Query("SELECT c FROM Country c WHERE " +
           "LOWER(c.nameOfCounty) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.serbianName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.nameOfCounty")
    List<Country> findByNameOrSerbianNameContainingIgnoreCase(@Param("keyword") String keyword);

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

    
    @Query(
            value = "SELECT DISTINCT c.* " +
                    "FROM countries c " +
                    "JOIN country_continents cc ON c.id = cc.country_id " +
                    "WHERE cc.continent IN :continents",
            nativeQuery = true
    )
    List<Country> findByAnyContinentIn(@Param("continents") List<String> continents);
    
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
