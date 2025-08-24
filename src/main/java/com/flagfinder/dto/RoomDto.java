package com.flagfinder.dto;

import com.flagfinder.enumeration.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomDto extends BaseEntityDto {
    private String hostUserName;
    private String guestUserName;
    private RoomStatus status;
    private LocalDateTime gameStartedAt;
    private LocalDateTime gameEndedAt;
} 