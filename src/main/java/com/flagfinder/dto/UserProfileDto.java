package com.flagfinder.dto;

import lombok.Data;

/**
 * A Data Transfer Object (DTO) for transferring user profile data.
 * Returns only the game name since email is already available from JWT.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
public class UserProfileDto {
    /**
     * The user's game name (display name).
     */
    private String gameName;
}
