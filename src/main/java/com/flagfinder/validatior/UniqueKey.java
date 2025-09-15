package com.flagfinder.validatior;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure uniqueness of game names.
 * Validates that a game name is not already taken by another user in the system.
 * Uses case-insensitive comparison for validation.
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueKeyValidator.class)
@Documented
public @interface UniqueKey {
    /**
     * The error message to display when validation fails.
     *
     * @return the validation error message
     */
    String message() default "Key is already taken";
    
    /**
     * Validation groups for conditional validation.
     *
     * @return array of validation groups
     */
    Class<?>[] groups() default {};
    
    /**
     * Payload for validation metadata.
     *
     * @return array of payload classes
     */
    Class<? extends Payload>[] payload() default {};
}