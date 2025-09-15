package com.flagfinder.repository;

import com.flagfinder.model.Friendship;
import com.flagfinder.model.Room;
import com.flagfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Room entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for room management including host/guest queries and status filtering.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Finds a room by its unique identifier.
     *
     * @param roomId the UUID of the room
     * @return Optional containing the room if found
     */
    Optional<Room> findOneById(UUID roomId);

    /**
     * Finds a single room where the user is the host.
     *
     * @param host the User who is hosting the room
     * @return Optional containing the room if found
     */
    Optional<Room> findOneByHost(User host);
    
    /**
     * Finds all rooms where the user is the host.
     *
     * @param host the User who is hosting the rooms
     * @return list of rooms hosted by the user
     */
    List<Room> findByHost(User host);
    
    /**
     * Finds rooms hosted by a user that are not in active or completed status.
     * Excludes rooms with status 'GAME_IN_PROGRESS' or 'GAME_COMPLETED'.
     *
     * @param host the User who is hosting the rooms
     * @return list of non-active/completed rooms hosted by the user
     */
    @Query("SELECT r FROM Room r WHERE r.host = :host AND r.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<Room> findByHostAndStatusNotInActiveOrCompleted(@Param("host") User host);

    /**
     * Finds a single room where the user is the guest.
     *
     * @param guest the User who is the guest in the room
     * @return Optional containing the room if found
     */
    Optional<Room> findOneByGuest(User guest);
    
    /**
     * Finds all rooms where the user is the guest.
     *
     * @param guest the User who is the guest in the rooms
     * @return list of rooms where the user is a guest
     */
    List<Room> findByGuest(User guest);
    
    /**
     * Finds rooms where the user is a guest and not in active or completed status.
     * Excludes rooms with status 'GAME_IN_PROGRESS' or 'GAME_COMPLETED'.
     *
     * @param guest the User who is the guest in the rooms
     * @return list of non-active/completed rooms where the user is a guest
     */
    @Query("SELECT r FROM Room r WHERE r.guest = :guest AND r.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<Room> findByGuestAndStatusNotInActiveOrCompleted(@Param("guest") User guest);

    /**
     * Finds a room where the user is either host or guest.
     *
     * @param user the User to search for as host or guest
     * @return Optional containing the room if found
     */
    @Query("SELECT r FROM Room r WHERE r.host = :user OR r.guest = :user")
    Optional<Room> findOneByUser(@Param("user") User user);

    /**
     * Finds a friendship between host and guest users.
     * Note: This method seems misplaced in RoomRepository and should likely be in FriendshipRepository.
     *
     * @param host the host user
     * @param guest the guest user
     * @return Optional containing the friendship if found
     */
    Optional<Friendship> findByHostAndGuest(User host, User guest);
}
