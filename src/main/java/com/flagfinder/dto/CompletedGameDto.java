package com.flagfinder.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompletedGameDto {
    private UUID roomId;
    private String hostUserName;
    private String guestUserName;
    private String winnerUserName;
    private Integer hostScore;
    private Integer guestScore;
    private Integer totalRounds;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
} 