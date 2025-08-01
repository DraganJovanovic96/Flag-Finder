package com.flagfinder.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * A Data Transfer Object (DTO) for transferring user data between layers of the application.
 * It extends the {@link BaseEntityDto} class.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
public class UserUpdateDto extends BaseEntityDto {
    /**
     * The user's email address.
     */
    private String email;

    /**
     * The user's first name.
     */
    private String firstname;

    /**
     * The user's last name.
     */
    private String lastname;

    /**
     * The user's mobile number.
     */
    private String mobileNumber;

    /**
     * The user's date of birth.
     */
    private LocalDate dateOfBirth;

    /**
     * The user's mobile address.
     */
    private String address;

    /**
     * This variable stores a 'String' that contains the URL of an image file.
     * The URL can be used to retrieve the image and display it in an application or on webpage.
     */
    private String imageUrl;
}
