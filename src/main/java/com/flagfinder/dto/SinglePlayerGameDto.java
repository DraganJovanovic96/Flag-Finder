package com.flagfinder.dto;

import com.flagfinder.enumeration.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SinglePlayerGameDto {
    private UUID id;
    private UUID roomId;
    private String playerName;
    private String hostName;
    private Integer hostScore;
    private Integer totalRounds;
    private Integer currentRound;
    private GameStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private SinglePlayerRoundDto currentSinglePlayerRoundData;
}
