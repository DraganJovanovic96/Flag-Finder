package com.flagfinder.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for joining an existing room request.
 * Contains the room identifier that the user wants to join.
 */
@Data
public class JoinRoomRequestDto extends BaseEntityDto {
    /**
     * The unique identifier of the room to join.
     */
    private UUID roomId;
}
