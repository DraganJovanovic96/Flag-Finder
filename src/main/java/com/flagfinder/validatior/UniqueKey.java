package com.flagfinder.validatior;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueKeyValidator.class)
@Documented
public @interface UniqueKey {
    String message() default "Key is already taken";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}