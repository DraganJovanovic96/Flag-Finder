package com.flagfinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for password change.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDto {

    /**
     * The password of the user.
     */
    private String password;

    /**
     * The new password of the user.
     */
    private String newPassword;

    /**
     * The repeat of the new password of the user.
     */
    private String repeatNewPassword;
}
