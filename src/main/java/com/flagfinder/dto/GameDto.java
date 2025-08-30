package com.flagfinder.dto;

import com.flagfinder.enumeration.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class GameDto {
    private UUID id;
    private UUID roomId;
    private List<String> playerNames;
    private String hostName;
    private String guestName;
    private Integer hostScore;
    private Integer guestScore;
    private Integer totalRounds;
    private Integer currentRound;
    private GameStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String winnerUserName;
    private RoundDto currentRoundData;
}
