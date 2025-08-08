package com.flagfinder.validatior;

import com.flagfinder.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueKeyValidator implements ConstraintValidator<UniqueKey, String> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(String userName, ConstraintValidatorContext context) {
        if (userName == null || userName.trim().isEmpty()) {
            return true;
        }
        return !userRepository.existsByUserNameIgnoreCase(userName);
    }
}