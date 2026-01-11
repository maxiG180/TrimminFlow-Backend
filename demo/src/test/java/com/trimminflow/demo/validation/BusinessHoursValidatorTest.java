package com.trimminflow.demo.validation;

import com.trimminflow.demo.dto.SetBusinessHoursRequest;
import com.trimminflow.demo.entity.DayOfWeek;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BusinessHoursValidatorTest {

    private BusinessHoursValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BusinessHoursValidator();
    }

    @Test
    void validateBusinessHours_ValidHours_Passes() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(18, 0));

        // When/Then
        assertDoesNotThrow(() -> validator.validateBusinessHours(request));
    }

    @Test
    void validateBusinessHours_ClosedDay_Passes() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.SUNDAY);
        request.setIsOpen(false);

        // When/Then
        assertDoesNotThrow(() -> validator.validateBusinessHours(request));
    }

    @Test
    void validateBusinessHours_MissingOpenTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(null);
        request.setCloseTime(LocalTime.of(18, 0));

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateBusinessHours(request));
    }

    @Test
    void validateBusinessHours_CloseBeforeOpen_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(18, 0));
        request.setCloseTime(LocalTime.of(9, 0));

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateBusinessHours(request));
    }

    @Test
    void validateBusinessHours_SameTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(9, 0));

        // When/Then
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateBusinessHours(request));
    }

    @Test
    void validateBusinessHours_EarlyMorning_Passes() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(6, 0));
        request.setCloseTime(LocalTime.of(14, 0));

        // When/Then
        assertDoesNotThrow(() -> validator.validateBusinessHours(request));
    }
}
