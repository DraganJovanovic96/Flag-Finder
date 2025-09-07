package com.flagfinder.repository;

import com.flagfinder.model.Country;
import com.flagfinder.model.Guess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GuessRepository extends JpaRepository<Guess,UUID> {

  List<Guess> findAllByRoundIdAndUserId(UUID roundId, UUID userId);

  @Query("SELECT g FROM Guess g WHERE g.round.id = :roundId AND g.user.gameName = :gameName")
  Guess findOneByRoundIdAndGameName(@Param("roundId") UUID roundId, @Param("gameName") String gameName);

  void deleteByGuessedCountry(Country country);
}
