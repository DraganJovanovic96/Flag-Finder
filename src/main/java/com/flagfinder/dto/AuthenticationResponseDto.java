package com.flagfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing an authentication response.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {
    /**
     * The access token generated upon successful authentication.
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * The refresh token used for obtaining a new access token.
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
}
