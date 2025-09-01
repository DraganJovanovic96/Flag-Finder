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

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Optional<Room> findOneById(UUID roomId);

    Optional<Room> findOneByHost(User host);
    
    List<Room> findByHost(User host);
    
    @Query("SELECT r FROM Room r WHERE r.host = :host AND r.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<Room> findByHostAndStatusNotInActiveOrCompleted(@Param("host") User host);

    Optional<Room> findOneByGuest(User guest);
    
    List<Room> findByGuest(User guest);
    
    @Query("SELECT r FROM Room r WHERE r.guest = :guest AND r.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<Room> findByGuestAndStatusNotInActiveOrCompleted(@Param("guest") User guest);

    @Query("SELECT r FROM Room r WHERE r.host = :user OR r.guest = :user")
    Optional<Room> findOneByUser(@Param("user") User user);

    Optional<Friendship> findByHostAndGuest(User host, User guest);
}
