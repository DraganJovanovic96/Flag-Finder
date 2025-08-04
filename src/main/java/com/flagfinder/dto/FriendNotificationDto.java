package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendNotificationDto {
    private String message;
    private UUID fromUserId;
    private String fromUserUsername;
}
