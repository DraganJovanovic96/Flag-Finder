package com.flagfinder.repository;

import com.flagfinder.model.SinglePlayerRoom;
import com.flagfinder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SinglePlayerRoomRepository extends JpaRepository<SinglePlayerRoom, UUID> {

    Optional<SinglePlayerRoom> findOneById(UUID singlePlayerRoomId);

    Optional<SinglePlayerRoom> findOneByHost(User host);

    List<SinglePlayerRoom> findByHost(User host);

    @Query("SELECT s FROM SinglePlayerRoom s WHERE s.host = :host AND s.status NOT IN ('GAME_IN_PROGRESS', 'GAME_COMPLETED')")
    List<SinglePlayerRoom> findByHostAndStatusNotInActiveOrCompleted(@Param("host") User host);
}
