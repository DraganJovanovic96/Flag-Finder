package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class GameInviteDto {

    private String initiatorUserName;

    private String targetUserName;

    private UUID gameId;
}
