package com.flagfinder.dto;


import com.flagfinder.enumeration.Role;
import com.flagfinder.validatior.UniqueKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a register request.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    /**
     * The username of the user.
     */
    @NotBlank(message = "Username is required")
    @NotNull(message = "Username is required")
    @UniqueKey(message = "Username is already taken")
    private String userName;


    /**
     * The first name of the user.
     */
    private String firstname;

    /**
     * The last name of the user.
     */
    private String lastname;

    /**
     * The email of the user.
     */
    @NotBlank(message = "Email is required")
    @NotNull(message = "Email is required")
    @UniqueKey(message = "Email is already taken")
    private String email;

    /**
     * The password of the user.
     */
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$", message = "Password must be at least 6 characters long and contain at least one letter and one number.")
    private String password;

    /**
     * The role of the user.
     */
    private Role role = Role.USER;

    /**
     * The mobile number of the user.
     */
    @NotNull
    private String mobileNumber;

    /**
     * The date of birth of the user.
     */
    private LocalDate dateOfBirth;

    /**
     * The address of the user.
     */
    private String address;

    /**
     * The URL of the user's profile image.
     */
    private String imageUrl;
}
