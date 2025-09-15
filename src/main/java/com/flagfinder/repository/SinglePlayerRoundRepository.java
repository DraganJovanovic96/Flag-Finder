package com.flagfinder.repository;

import com.flagfinder.model.Country;
import com.flagfinder.model.Round;
import com.flagfinder.model.SinglePlayerRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SinglePlayerRound entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for single player round management including game-based queries and country associations.
 */
@Repository
public interface SinglePlayerRoundRepository extends JpaRepository<SinglePlayerRound, UUID> {

    /**
     * Finds a single player round by its unique identifier.
     *
     * @param singlePlayerRoundId the UUID of the single player round
     * @return Optional containing the round if found
     */
    Optional<SinglePlayerRound> findOneById(UUID singlePlayerRoundId);
    
    /**
     * Finds the latest round for a specific single player game by round number.
     * Returns the round with the highest round number for the game.
     *
     * @param singlePlayerGameId the UUID of the single player game
     * @return Optional containing the latest round if found
     */
    @Query("SELECT r FROM SinglePlayerRound r WHERE r.singlePlayerGame.id = :singlePlayerGameId ORDER BY r.roundNumber DESC LIMIT 1")
    Optional<SinglePlayerRound> findLatestRoundByGameId(@Param("singlePlayerGameId") UUID singlePlayerGameId);
    
    /**
     * Finds all rounds for a specific single player game ordered by round number.
     * Returns rounds in ascending order from first to last.
     *
     * @param singlePlayerGameId the UUID of the single player game
     * @return list of rounds ordered by round number
     */
    @Query("SELECT r FROM SinglePlayerRound r WHERE r.singlePlayerGame.id = :singlePlayerGameId ORDER BY r.roundNumber ASC")
    List<SinglePlayerRound> findByGameIdOrderByRoundNumber(@Param("singlePlayerGameId") UUID singlePlayerGameId);

    /**
     * Deletes all single player rounds associated with a specific country.
     * Used for cascade deletion when a country is removed.
     *
     * @param country the Country entity to delete rounds for
     */
    void deleteByCountry(Country country);
    
    /**
     * Finds all single player rounds that feature a specific country.
     *
     * @param country the Country entity to find rounds for
     * @return list of single player rounds featuring the country
     */
    List<SinglePlayerRound> findByCountry(Country country);
}
