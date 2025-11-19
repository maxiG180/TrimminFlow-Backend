package com.trimminflow.demo.validation;

import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.BusinessHours;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Validator implementation for @ValidBusinessHours annotation
 *
 * Validates business hours logic:
 * 1. If shop is open, both open and close times must be provided
 * 2. Open time must be before close time
 * 3. Minimum duration of 1 hour between open and close
 *
 * Works with both BusinessHours entity and SetBusinessHoursRequest DTO
 */
public class BusinessHoursValidator implements ConstraintValidator<ValidBusinessHours, Object> {

    @Override
    public void initialize(ValidBusinessHours constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        // If object is null, let @NotNull handle it
        if (obj == null) {
            return true;
        }

        // Extract fields based on object type
        Boolean isOpen;
        LocalTime openTime;
        LocalTime closeTime;

        if (obj instanceof BusinessHours) {
            BusinessHours hours = (BusinessHours) obj;
            isOpen = hours.getIsOpen();
            openTime = hours.getOpenTime();
            closeTime = hours.getCloseTime();
        } else if (obj instanceof SetBusinessHoursRequest) {
            SetBusinessHoursRequest request = (SetBusinessHoursRequest) obj;
            isOpen = request.getIsOpen();
            openTime = request.getOpenTime();
            closeTime = request.getCloseTime();
        } else {
            // Unknown type, skip validation
            return true;
        }

        // If shop is closed, no time validation needed
        if (isOpen == null || !isOpen) {
            return true;
        }

        // Disable default message
        context.disableDefaultConstraintViolation();

        // Rule 1: If open, both times must be provided
        if (openTime == null || closeTime == null) {
            context.buildConstraintViolationWithTemplate(
                "Open time and close time are required when the shop is marked as open"
            ).addPropertyNode("openTime").addConstraintViolation();
            return false;
        }

        // Rule 2: Open time must be before close time
        if (!openTime.isBefore(closeTime)) {
            context.buildConstraintViolationWithTemplate(
                "Open time must be before close time"
            ).addPropertyNode("closeTime").addConstraintViolation();
            return false;
        }

        // Rule 3: Minimum duration of 1 hour
        Duration duration = Duration.between(openTime, closeTime);
        if (duration.toMinutes() < 60) {
            context.buildConstraintViolationWithTemplate(
                "Business must be open for at least 1 hour (current duration: " + duration.toMinutes() + " minutes)"
            ).addPropertyNode("closeTime").addConstraintViolation();
            return false;
        }

        return true;
    }
}
