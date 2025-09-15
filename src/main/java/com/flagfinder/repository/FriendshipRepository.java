package com.flagfinder.repository;

import com.flagfinder.enumeration.FriendshipStatus;
import com.flagfinder.model.Friendship;
import com.flagfinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Friendship entity operations.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for friendship management including bidirectional relationship queries and status filtering.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    /**
     * Finds all friendships for a specific user with a given status.
     * Searches both as initiator and target of friendships.
     *
     * @param userId the UUID of the user
     * @param friendshipStatusm the friendship status to filter by
     * @param pageable pagination information
     * @return paginated list of friendships for the user
     */
    @Query("""
    SELECT f FROM Friendship f
    WHERE (f.initiator.id = :userId OR f.target.id = :userId)
    AND f.friendshipStatus = :friendshipStatus
""")
    Page<Friendship> findAllFriendshipsOfUser(@Param("userId") UUID userId, @Param("friendshipStatus") FriendshipStatus friendshipStatusm,  Pageable pageable);

    /**
     * Finds a friendship by specific initiator and target users.
     * Directional search - only matches exact initiator-target relationship.
     *
     * @param initiator the user who initiated the friendship
     * @param target the user who received the friendship request
     * @return Optional containing the friendship if found
     */
    Optional<Friendship> findByInitiatorAndTarget(User initiator, User target);

    /**
     * Finds a friendship between two users regardless of who initiated it.
     * Bidirectional search - matches both user1->user2 and user2->user1 relationships.
     *
     * @param user1 the first user in the relationship
     * @param user2 the second user in the relationship
     * @param status the friendship status to filter by
     * @return Optional containing the friendship if found
     */
    @Query("""
    SELECT f FROM Friendship f
    WHERE ((f.initiator = :user1 AND f.target = :user2) OR (f.initiator = :user2 AND f.target = :user1))
    AND f.friendshipStatus = :status
""")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("status") FriendshipStatus status);

    /**
     * Finds friendships where the specified user is the target with a given status.
     * Useful for finding incoming friend requests.
     *
     * @param target the target user of the friendships
     * @param friendshipStatus the friendship status to filter by
     * @param pageable pagination information
     * @return paginated list of friendships targeting the user
     */
    Page<Friendship> findByTargetAndFriendshipStatus(User target, FriendshipStatus friendshipStatus, Pageable pageable);
}
