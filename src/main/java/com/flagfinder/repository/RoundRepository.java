package com.flagfinder.repository;

import com.flagfinder.model.Country;
import com.flagfinder.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Round entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for round management including game-based queries and country associations.
 */
@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    /**
     * Finds a round by its unique identifier.
     *
     * @param serviceId the UUID of the round
     * @return Optional containing the round if found
     */
    Optional<Round> findOneById(UUID serviceId);
    
    /**
     * Finds the latest round for a specific game by round number.
     * Returns the round with the highest round number for the game.
     *
     * @param gameId the UUID of the game
     * @return Optional containing the latest round if found
     */
    @Query("SELECT r FROM Round r WHERE r.game.id = :gameId ORDER BY r.roundNumber DESC LIMIT 1")
    Optional<Round> findLatestRoundByGameId(@Param("gameId") UUID gameId);
    
    /**
     * Finds all rounds for a specific game ordered by round number.
     * Returns rounds in ascending order from first to last.
     *
     * @param gameId the UUID of the game
     * @return list of rounds ordered by round number
     */
    @Query("SELECT r FROM Round r WHERE r.game.id = :gameId ORDER BY r.roundNumber ASC")
    List<Round> findByGameIdOrderByRoundNumber(@Param("gameId") UUID gameId);

    /**
     * Deletes all rounds associated with a specific country.
     * Used for cascade deletion when a country is removed.
     *
     * @param country the Country entity to delete rounds for
     */
    void deleteByCountry(Country country);
    
    /**
     * Finds all rounds that feature a specific country.
     *
     * @param country the Country entity to find rounds for
     * @return list of rounds featuring the country
     */
    List<Round> findByCountry(Country country);
}
