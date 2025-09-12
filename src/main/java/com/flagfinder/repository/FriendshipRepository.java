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

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
    SELECT f FROM Friendship f
    WHERE (f.initiator.id = :userId OR f.target.id = :userId)
    AND f.friendshipStatus = :friendshipStatus
""")
    Page<Friendship> findAllFriendshipsOfUser(@Param("userId") UUID userId, @Param("friendshipStatus") FriendshipStatus friendshipStatusm,  Pageable pageable);

    Optional<Friendship> findByInitiatorAndTarget(User initiator, User target);

    @Query("""
    SELECT f FROM Friendship f
    WHERE ((f.initiator = :user1 AND f.target = :user2) OR (f.initiator = :user2 AND f.target = :user1))
    AND f.friendshipStatus = :status
""")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("status") FriendshipStatus status);

    Page<Friendship> findByTargetAndFriendshipStatus(User target, FriendshipStatus friendshipStatus, Pageable pageable);
}
