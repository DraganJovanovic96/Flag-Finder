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

@Repository
public interface SinglePlayerRoundRepository extends JpaRepository<SinglePlayerRound, UUID> {

    Optional<SinglePlayerRound> findOneById(UUID singlePlayerRoundId);
    
    @Query("SELECT r FROM SinglePlayerRound r WHERE r.singlePlayerGame.id = :singlePlayerGameId ORDER BY r.roundNumber DESC LIMIT 1")
    Optional<SinglePlayerRound> findLatestRoundByGameId(@Param("singlePlayerGameId") UUID singlePlayerGameId);
    
    @Query("SELECT r FROM SinglePlayerRound r WHERE r.singlePlayerGame.id = :singlePlayerGameId ORDER BY r.roundNumber ASC")
    List<SinglePlayerRound> findByGameIdOrderByRoundNumber(@Param("singlePlayerGameId") UUID singlePlayerGameId);

    void deleteByCountry(Country country);
}
