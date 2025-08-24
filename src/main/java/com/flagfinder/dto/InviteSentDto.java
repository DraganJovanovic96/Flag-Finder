package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class InviteSentDto {
    private String guestUserName;

    private UUID roomId;
}
