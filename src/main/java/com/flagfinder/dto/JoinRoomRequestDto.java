package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class JoinRoomRequestDto extends BaseEntityDto {
    private UUID roomId;
}
