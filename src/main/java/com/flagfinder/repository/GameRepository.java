package com.flagfinder.repository;

import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.model.Game;
import com.flagfinder.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Game entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for game management including user statistics, multiplayer filtering, and relationship fetching.
 */
@Repository
public interface GameRepository extends JpaRepository <Game, UUID> {
    /**
     * Finds a game by room and status.
     *
     * @param room the room associated with the game
     * @param status the game status to filter by
     * @return the Game matching the criteria
     */
    Game findByRoomAndStatus(Room room, GameStatus status);

    /**
     * Finds a game by ID with eagerly loaded relationships.
     * Uses LEFT JOIN FETCH to avoid N+1 query problems.
     *
     * @param gameId the UUID of the game
     * @return Optional containing the game with loaded relationships
     */
    @Query("SELECT DISTINCT g FROM Game g " +
           "LEFT JOIN FETCH g.room " +
           "LEFT JOIN FETCH g.users " +
           "WHERE g.id = :gameId")
    Optional<Game> findByIdWithRelations(@Param("gameId") UUID gameId);

    /**
     * Counts the number of games won by a specific user (case insensitive).
     *
     * @param userName the username to count wins for
     * @return the number of games won by the user
     */
    long countByWinnerUserNameIgnoreCase(String userName);

    /**
     * Finds recent games for a specific user ordered by creation date.
     *
     * @param userName the username to find games for
     * @param pageable pagination information for limiting results
     * @return list of recent games for the user
     */
    @Query("""
    SELECT g FROM Game g
    JOIN g.users u
    WHERE LOWER(u.gameName) = LOWER(:userName)
    ORDER BY g.createdAt DESC
    """)
    List<Game> findRecentGamesByUser(@Param("userName") String userName, Pageable pageable);

    /**
     * Finds all multiplayer games for a specific user.
     * Filters games with more than one user (multiplayer only).
     *
     * @param userName the username to find games for
     * @return list of multiplayer games for the user
     */
    @Query("""
SELECT DISTINCT g FROM Game g
JOIN g.users u
WHERE LOWER(u.gameName) = LOWER(:userName)
AND SIZE(g.users) > 1
ORDER BY g.createdAt DESC
""")
    List<Game> findAllMultiplayerByUser(@Param("userName") String userName);

    /**
     * Finds all multiplayer games for a specific user with pagination.
     * Filters games with more than one user (multiplayer only).
     *
     * @param userName the username to find games for
     * @param pageable pagination information
     * @return paginated list of multiplayer games for the user
     */
    @Query("""
SELECT DISTINCT g FROM Game g
JOIN g.users u
WHERE LOWER(u.gameName) = LOWER(:userName)
AND SIZE(g.users) > 1
ORDER BY g.createdAt DESC
""")
    Page<Game> findAllMultiplayerByUser(@Param("userName") String userName, Pageable pageable);

    /**
     * Counts the number of multiplayer games won by a specific user.
     * Only considers games with more than one user and where the user is the winner.
     *
     * @param userName the username to count wins for
     * @return the number of multiplayer games won by the user
     */
    @Query("""
SELECT COUNT(DISTINCT g) FROM Game g
JOIN g.users u
WHERE LOWER(u.gameName) = LOWER(:userName)
AND SIZE(g.users) > 1
AND LOWER(g.winnerUserName) = LOWER(:userName)
""")
    Long countWonGamesByUser(@Param("userName") String userName);

    /**
     * Counts the number of multiplayer games that ended in a draw for a specific user.
     * Only considers games with more than one user and no winner.
     *
     * @param userName the username to count draws for
     * @return the number of multiplayer games that ended in a draw
     */
    @Query("""
SELECT COUNT(DISTINCT g) FROM Game g
JOIN g.users u
WHERE LOWER(u.gameName) = LOWER(:userName)
AND SIZE(g.users) > 1
AND g.winnerUserName IS NULL
""")
    Long countDrawGamesByUser(@Param("userName") String userName);

    /**
     * Finds completed multiplayer games for a user with specific status, ordered by end date.
     * Only includes games that have ended and are multiplayer.
     *
     * @param userName the username to find games for
     * @param status the game status to filter by
     * @return list of completed games ordered by end date
     */
    @Query("""
SELECT DISTINCT g FROM Game g
JOIN g.users u
WHERE LOWER(u.gameName) = LOWER(:userName)
AND g.status = :status
AND SIZE(g.users) > 1
AND g.endedAt IS NOT NULL
ORDER BY g.endedAt ASC
""")
    List<Game> findByUserAndStatusOrderByGameEndedAtAsc(@Param("userName") String userName, @Param("status") GameStatus status);
}
