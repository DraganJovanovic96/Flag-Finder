package com.flagfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The UserFiltersQueryDto class represents a data transfer object that contains query attributes for filtering User entities.
 * These attributes include the User's first and last name, email, address and phone number.
 * <p>
 * The class also uses Jackson's @JsonProperty annotation to specify the JSON property names for each attribute.
 *
 * @author Dragan Jovanovic
 * @version 1.0
 * @since 1.0
 */
@Data
public class UserFiltersQueryDto {
    /**
     * The customer's full name.
     */
    @JsonProperty("fullName")
    private String fullName;

    /**
     * The customer's address.
     */
    @JsonProperty("address")
    private String address;

    /**
     * The customer's address.
     */
    @JsonProperty("email")
    private String email;

    /**
     * The customer's phoneNumber.
     */
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    /**
     * The customer deletion status.
     */
    @JsonProperty("isDeleted")
    private boolean isDeleted;
}
