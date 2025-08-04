package com.flagfinder.repository;

import com.flagfinder.enumeration.FriendshipStatus;
import com.flagfinder.model.Friendship;
import com.flagfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
    SELECT f FROM Friendship f
    WHERE (f.initiator.id = :userId OR f.target.id = :userId)
    AND f.status = :status
""")
    List<Friendship> findAllFriendshipsOfUser(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);

    Optional<Friendship> findByInitiatorAndTarget(User initiator, User target);

    List<Friendship> findByTargetAndStatus(User target, FriendshipStatus status);

}
