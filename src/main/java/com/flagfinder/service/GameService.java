package com.flagfinder.service;

import com.flagfinder.dto.SendUserNameDto;
import com.flagfinder.model.Game;

import java.util.List;
import java.util.UUID;

public interface GameService {

    Game inviteToPlay(SendUserNameDto sendUserNameDto);
    
    /**
     * Gets a game by ID
     */
    Game getGame(UUID gameId);
    
    /**
     * Gets all games for a user
     */
    List<Game> getGamesByUser(String userName);
    
    /**
     * Gets all completed games
     */
    List<Game> getAllCompletedGames();
}
