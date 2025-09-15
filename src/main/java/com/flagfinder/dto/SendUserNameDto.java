package com.flagfinder.dto;

import lombok.Data;

/**
 * DTO for sending username information.
 * Contains the username to be transmitted.
 */
@Data
public class SendUserNameDto {
    /**
     * The username to be sent.
     */
    private String userName;
}
