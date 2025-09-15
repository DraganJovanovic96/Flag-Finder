package com.flagfinder.repository;

import com.flagfinder.model.Country;
import com.flagfinder.model.Guess;
import com.flagfinder.model.Round;
import com.flagfinder.model.SinglePlayerRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Guess entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for guess management including round-based queries and cascade deletions.
 */
public interface GuessRepository extends JpaRepository<Guess,UUID> {

  /**
   * Finds all guesses for a specific round and user.
   *
   * @param roundId the UUID of the round
   * @param userId the UUID of the user
   * @return list of guesses for the round and user
   */
  List<Guess> findAllByRoundIdAndUserId(UUID roundId, UUID userId);

  /**
   * Finds a single guess by round ID and user's game name.
   *
   * @param roundId the UUID of the round
   * @param gameName the game name of the user
   * @return the Guess for the specified round and user
   */
  @Query("SELECT g FROM Guess g WHERE g.round.id = :roundId AND g.user.gameName = :gameName")
  Guess findOneByRoundIdAndGameName(@Param("roundId") UUID roundId, @Param("gameName") String gameName);

  /**
   * Deletes all guesses associated with a specific country.
   * Used for cascade deletion when a country is removed.
   *
   * @param country the Country entity to delete guesses for
   */
  void deleteByGuessedCountry(Country country);
  
  /**
   * Deletes all guesses associated with a specific round.
   * Used for cascade deletion when a round is removed.
   *
   * @param round the Round entity to delete guesses for
   */
  void deleteByRound(Round round);
  
  /**
   * Deletes all guesses associated with a specific single player round.
   * Used for cascade deletion when a single player round is removed.
   *
   * @param singlePlayerRound the SinglePlayerRound entity to delete guesses for
   */
  void deleteBySinglePlayerRound(SinglePlayerRound singlePlayerRound);
}
