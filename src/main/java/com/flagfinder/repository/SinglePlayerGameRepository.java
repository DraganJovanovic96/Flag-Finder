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

/**
 * Repository interface for SinglePlayerGame entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for single player game management including room associations and user queries.
 */
@Repository
public interface SinglePlayerGameRepository extends JpaRepository <SinglePlayerGame, UUID> {
    /**
     * Finds a single player game by room and status.
     *
     * @param singlePlayerRoom the SinglePlayerRoom associated with the game
     * @param status the game status to filter by
     * @return the SinglePlayerGame matching the criteria
     */
    SinglePlayerGame findBySinglePlayerRoomAndStatus(SinglePlayerRoom singlePlayerRoom, GameStatus status);
    
    /**
     * Finds a single player game by its associated room.
     *
     * @param singlePlayerRoom the SinglePlayerRoom to find the game for
     * @return Optional containing the single player game if found
     */
    Optional<SinglePlayerGame> findBySinglePlayerRoom(SinglePlayerRoom singlePlayerRoom);
    
    /**
     * Finds a single player game by ID with eagerly loaded relationships.
     * Uses LEFT JOIN FETCH to avoid N+1 query problems.
     *
     * @param singlePlayerGameId the UUID of the single player game
     * @return Optional containing the game with loaded relationships
     */
    @Query("SELECT DISTINCT s FROM SinglePlayerGame s " +
           "LEFT JOIN FETCH s.singlePlayerRoom " +
           "LEFT JOIN FETCH s.user " +
           "WHERE s.id = :singlePlayerGameId")
    Optional<SinglePlayerGame> findByIdWithRelations(@Param("singlePlayerGameId") UUID singlePlayerGameId);

    /**
     * Finds recent single player games for a specific user ordered by creation date.
     *
     * @param userName the username to find games for
     * @param pageable pagination information for limiting results
     * @return list of recent single player games for the user
     */
    @Query("""
    SELECT s FROM SinglePlayerGame s
    JOIN s.user u
    WHERE LOWER(u.gameName) = LOWER(:userName)
    ORDER BY s.createdAt DESC
    """)
    List<SinglePlayerGame> findRecentSinglePlayerGamesByUser(@Param("userName") String userName, Pageable pageable);
}
