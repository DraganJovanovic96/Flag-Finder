package com.flagfinder.repository;

import com.flagfinder.model.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    Optional<Round> findOneById(UUID serviceId);
    
    @Query("SELECT r FROM Round r WHERE r.game.id = :gameId ORDER BY r.roundNumber DESC LIMIT 1")
    Optional<Round> findLatestRoundByGameId(@Param("gameId") UUID gameId);
    
    @Query("SELECT r FROM Round r WHERE r.game.id = :gameId ORDER BY r.roundNumber ASC")
    List<Round> findByGameIdOrderByRoundNumber(@Param("gameId") UUID gameId);
}
