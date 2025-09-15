package com.flagfinder.repository;

import com.flagfinder.model.SinglePlayerRoom;
import com.flagfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SinglePlayerRoom entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for single player room management including host queries and status filtering.
 */
public interface SinglePlayerRoomRepository extends JpaRepository<SinglePlayerRoom, UUID> {

    /**
     * Finds a single player room by its unique identifier.
     *
     * @param singlePlayerRoomId the UUID of the single player room
     * @return Optional containing the room if found
     */
    Optional<SinglePlayerRoom> findOneById(UUID singlePlayerRoomId);

    /**
     * Finds a single player room where the user is the host.
     *
     * @param host the User who is hosting the room
     * @return Optional containing the room if found
     */
    Optional<SinglePlayerRoom> findOneByHost(User host);

    /**
     * Finds all single player rooms where the user is the host.
     *
     * @param host the User who is hosting the rooms
     * @return list of single player rooms hosted by the user
     */
    List<SinglePlayerRoom> findByHost(User host);

    /**
     * Finds single player rooms hosted by a user that are not in active or completed status.
     * Excludes rooms with status 'GAME_IN_PROGRESS' or 'GAME_COMPLETED'.
     *
     * @param host the User who is hosting the rooms
     * @return list of non-active/completed single player rooms hosted by the user
     */
    @Query("SELECT s FROM SinglePlayerRoom s WHERE s.host = :host AND s.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<SinglePlayerRoom> findByHostAndStatusNotInActiveOrCompleted(@Param("host") User host);
}
