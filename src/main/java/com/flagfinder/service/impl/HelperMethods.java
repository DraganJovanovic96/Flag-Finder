package com.flagfinder.service.impl;

import com.flagfinder.dto.UserInfoDto;
import com.flagfinder.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelperMethods {

    private final GameService gameService;

    public UserInfoDto setPlayerInfoCard(String gameName) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setAccuracyPercentage(gameService.accuracyPercentage(gameName));
        userInfoDto.setNumberOfWonGame(gameService.countOfWinningGames(gameName));
        userInfoDto.setBestStreak(gameService.getBestWinningStreak(gameName));

        return userInfoDto;
    }
}
