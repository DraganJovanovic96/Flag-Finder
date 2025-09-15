package com.flagfinder.dto;

import com.flagfinder.service.GameService;
import lombok.Data;

@Data
public class UserInfoDto {
    Long numberOfWonGame;
    int accuracyPercentage;
    int bestStreak;
}
