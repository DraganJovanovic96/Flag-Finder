package com.flagfinder.service.impl;

import com.flagfinder.dto.UserInfoDto;
import com.flagfinder.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Helper service providing utility methods for user information and statistics.
 * Contains reusable methods for generating user profile cards and aggregating game statistics.
 */
@Service
@RequiredArgsConstructor
public class HelperMethods {

    private final GameService gameService;

    /**
     * Creates a comprehensive user information card with game statistics.
     * Aggregates player performance data including accuracy, wins, and streaks.
     *
     * @param gameName the game name of the player to generate info card for
     * @return UserInfoDto containing player's game statistics and performance metrics
     */
    public UserInfoDto setPlayerInfoCard(String gameName) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setAccuracyPercentage(gameService.accuracyPercentage(gameName));
        userInfoDto.setNumberOfWonGame(gameService.countOfWinningGames(gameName));
        userInfoDto.setBestStreak(gameService.getBestWinningStreak(gameName));

        return userInfoDto;
    }
}
