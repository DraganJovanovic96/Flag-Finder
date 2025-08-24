package com.flagfinder.repository;

import com.flagfinder.model.Friendship;
import com.flagfinder.model.Room;
import com.flagfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findOneById(UUID roomId);

    Optional<Room> findOneByHost(User host);

    Optional<Room> findOneByGuest(User guest);

    @Query("SELECT r FROM Room r WHERE r.host = :user OR r.guest = :user")
    Optional<Room> findOneByUser(@Param("user") User user);

    Optional<Friendship> findByHostAndGuest(User host, User guest);
}
