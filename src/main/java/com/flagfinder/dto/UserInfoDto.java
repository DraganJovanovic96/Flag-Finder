package com.flagfinder.dto;

import com.flagfinder.service.GameService;
import lombok.Data;

/**
 * DTO for user game statistics and information.
 * Contains performance metrics and game history data.
 */
@Data
public class UserInfoDto {
    /**
     * The total number of games won by the user.
     */
    Long numberOfWonGame;
    
    /**
     * The user's accuracy percentage across all games.
     */
    int accuracyPercentage;
    
    /**
     * The user's best winning streak.
     */
    int bestStreak;
}
