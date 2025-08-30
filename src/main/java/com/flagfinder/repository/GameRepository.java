package com.flagfinder.repository;

import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.model.Game;
import com.flagfinder.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository <Game, UUID> {
    Game findByRoomAndStatus(Room room, GameStatus status);
}
