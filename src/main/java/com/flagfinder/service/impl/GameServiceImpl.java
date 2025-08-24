package com.flagfinder.service.impl;

import com.flagfinder.dto.SendUserNameDto;
import com.flagfinder.enumeration.GameStatus;
import com.flagfinder.model.Game;
import com.flagfinder.repository.GameRepository;
import com.flagfinder.repository.UserRepository;
import com.flagfinder.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {
    
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    
    @Override
    public Game inviteToPlay(SendUserNameDto sendUserNameDto) {
        // This method is kept for backward compatibility
        // In the new multiplayer system, this would be handled by the room system
        log.warn("inviteToPlay method is deprecated. Use room system instead.");
        return null;
    }
    
    @Override
    public Game getGame(UUID gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
    }
    
    @Override
    public List<Game> getGamesByUser(String userName) {
        // This would need a custom query in the repository
        // For now, we'll get all games and filter by user
        return gameRepository.findAll().stream()
                .filter(game -> game.getUsers().stream()
                        .anyMatch(user -> userName.equals(user.getGameName())))
                .toList();
    }
    
    @Override
    public List<Game> getAllCompletedGames() {
        return gameRepository.findAll().stream()
                .filter(game -> GameStatus.COMPLETED.equals(game.getStatus()))
                .toList();
    }
} 