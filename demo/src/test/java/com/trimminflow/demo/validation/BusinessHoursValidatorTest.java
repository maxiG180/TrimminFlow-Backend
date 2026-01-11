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
    void validate_WithValidOpenHours_Passes() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(18, 0));

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validate_WithClosedDay_Passes() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.SUNDAY);
        request.setIsOpen(false);
        request.setOpenTime(null);
        request.setCloseTime(null);

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validate_WithOpenDayMissingOpenTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(null);
        request.setCloseTime(LocalTime.of(18, 0));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
        assertTrue(exception.getMessage().contains("Open time is required"));
    }

    @Test
    void validate_WithOpenDayMissingCloseTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
        assertTrue(exception.getMessage().contains("Close time is required"));
    }

    @Test
    void validate_WithCloseTimeBeforeOpenTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(18, 0));
        request.setCloseTime(LocalTime.of(9, 0)); // Close before open

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
        assertTrue(exception.getMessage().contains("Close time must be after open time"));
    }

    @Test
    void validate_WithCloseTimeEqualToOpenTime_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(9, 0)); // Same time

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request));
        assertTrue(exception.getMessage().contains("Close time must be after open time"));
    }

    @Test
    void validate_WithNullDayOfWeek_ThrowsException() {
        // Given
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(null);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(18, 0));

        // When & Then
        assertThrows(Exception.class, () -> validator.validate(request));
    }

    @Test
    void validate_WithEarlyMorningHours_Passes() {
        // Given - Shop open from 6 AM to 2 PM
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(6, 0));
        request.setCloseTime(LocalTime.of(14, 0));

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validate_WithLateNightHours_Passes() {
        // Given - Shop open from 2 PM to 11 PM
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.FRIDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(14, 0));
        request.setCloseTime(LocalTime.of(23, 0));

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void validate_WithAllDayHours_Passes() {
        // Given - Shop open 24 hours (almost)
        SetBusinessHoursRequest request = new SetBusinessHoursRequest();
        request.setDayOfWeek(DayOfWeek.SATURDAY);
        request.setIsOpen(true);
        request.setOpenTime(LocalTime.of(0, 0));
        request.setCloseTime(LocalTime.of(23, 59));

        // When & Then
        assertDoesNotThrow(() -> validator.validate(request));
    }
}
