package com.flagfinder.validatior;

import com.flagfinder.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for the UniqueKey annotation.
 * Validates that game names are unique across all users in the system.
 * Performs case-insensitive validation to prevent duplicate game names.
 */
@Component
@RequiredArgsConstructor
public class UniqueKeyValidator implements ConstraintValidator<UniqueKey, String> {

    private final UserRepository userRepository;

    /**
     * Validates that the provided game name is unique in the system.
     * Performs case-insensitive check against existing user game names.
     * Null or empty values are considered valid (handled by other validators).
     *
     * @param userName the game name to validate for uniqueness
     * @param context the validation context
     * @return true if the game name is unique or null/empty, false if already taken
     */
    @Override
    public boolean isValid(String userName, ConstraintValidatorContext context) {
        if (userName == null || userName.trim().isEmpty()) {
            return true;
        }
        return !userRepository.existsByGameNameIgnoreCase(userName);
    }
}