package com.trimminflow.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for BusinessHours entity
 *
 * Validates:
 * - When isOpen = true, both openTime and closeTime must be provided
 * - openTime must be before closeTime
 * - Minimum business hours of 1 hour
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BusinessHoursValidator.class)
@Documented
public @interface ValidBusinessHours {
    String message() default "Invalid business hours configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
