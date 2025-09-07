package com.flagfinder.repository;

import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.model.Game;
import com.flagfinder.model.Room;
import com.flagfinder.model.SinglePlayerGame;
import com.flagfinder.model.SinglePlayerRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SinglePlayerGameRepository extends JpaRepository <SinglePlayerGame, UUID> {
    SinglePlayerGame findBySinglePlayerRoomAndStatus(SinglePlayerRoom singlePlayerRoom, GameStatus status);
    
    Optional<SinglePlayerGame> findBySinglePlayerRoom(SinglePlayerRoom singlePlayerRoom);
    
    @Query("SELECT DISTINCT s FROM SinglePlayerGame s " +
           "LEFT JOIN FETCH s.singlePlayerRoom " +
           "LEFT JOIN FETCH s.user " +
           "WHERE s.id = :singlePlayerGameId")
    Optional<SinglePlayerGame> findByIdWithRelations(@Param("singlePlayerGameId") UUID singlePlayerGameId);

    @Query("""
    SELECT s FROM SinglePlayerGame s
    JOIN s.user u
    WHERE LOWER(u.gameName) = LOWER(:userName)
    ORDER BY s.createdAt DESC
    """)
    List<SinglePlayerGame> findRecentSinglePlayerGamesByUser(@Param("userName") String userName, Pageable pageable);
}
