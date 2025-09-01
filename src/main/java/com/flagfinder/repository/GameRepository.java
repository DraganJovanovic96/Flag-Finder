package com.flagfinder.repository;

import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.model.Game;
import com.flagfinder.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository <Game, UUID> {
    Game findByRoomAndStatus(Room room, GameStatus status);
    
    @Query("SELECT DISTINCT g FROM Game g " +
           "LEFT JOIN FETCH g.room " +
           "LEFT JOIN FETCH g.users " +
           "WHERE g.id = :gameId")
    Optional<Game> findByIdWithRelations(@Param("gameId") UUID gameId);
}
