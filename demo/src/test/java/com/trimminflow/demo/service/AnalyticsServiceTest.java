package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.AnalyticsResponse;
import com.trimminflow.demo.entity.AppointmentStatus;
import com.trimminflow.demo.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private UUID barbershopId;

    @BeforeEach
    void setUp() {
        barbershopId = UUID.randomUUID();
    }

    @Test
    void getAnalytics_WithNoData_ReturnsZeroMetrics() {
        // Given
        when(appointmentRepository.countByBarbershopId(barbershopId)).thenReturn(0L);
        when(appointmentRepository.countByBarbershopIdAndStatus(eq(barbershopId), any())).thenReturn(0L);
        when(appointmentRepository.findCompletedAppointmentsWithPrice(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.findPopularServices(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.findBarberPerformance(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(eq(barbershopId), any(), any()))
                .thenReturn(0L);

        // When
        AnalyticsResponse response = analyticsService.getAnalytics(barbershopId, null);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getTotalAppointments());
        assertEquals(BigDecimal.ZERO, response.getTotalRevenue());
    }

    @Test
    void getAnalytics_WithCompletedAppointments_CalculatesRevenue() {
        // Given
        when(appointmentRepository.countByBarbershopId(barbershopId)).thenReturn(5L);
        when(appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.COMPLETED))
                .thenReturn(3L);
        when(appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.CANCELLED))
                .thenReturn(1L);
        when(appointmentRepository.countByBarbershopIdAndStatus(barbershopId, AppointmentStatus.NO_SHOW))
                .thenReturn(1L);

        Object[] price1 = { BigDecimal.valueOf(20.00) };
        Object[] price2 = { BigDecimal.valueOf(30.00) };
        when(appointmentRepository.findCompletedAppointmentsWithPrice(barbershopId))
                .thenReturn(Arrays.asList(price1, price2));

        when(appointmentRepository.findPopularServices(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.findBarberPerformance(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(eq(barbershopId), any(), any()))
                .thenReturn(0L);

        // When
        AnalyticsResponse response = analyticsService.getAnalytics(barbershopId, null);

        // Then
        assertEquals(5L, response.getTotalAppointments());
        assertEquals(3L, response.getCompletedAppointments());
        assertEquals(BigDecimal.valueOf(50.00), response.getTotalRevenue());
    }

    @Test
    void getAnalytics_WithPopularServices_ReturnsServiceStats() {
        // Given
        when(appointmentRepository.countByBarbershopId(barbershopId)).thenReturn(0L);
        when(appointmentRepository.countByBarbershopIdAndStatus(eq(barbershopId), any())).thenReturn(0L);
        when(appointmentRepository.findCompletedAppointmentsWithPrice(barbershopId)).thenReturn(List.of());

        Object[] service1 = { "Haircut", 5L, BigDecimal.valueOf(100.00) };
        Object[] service2 = { "Beard Trim", 3L, BigDecimal.valueOf(30.00) };
        when(appointmentRepository.findPopularServices(barbershopId))
                .thenReturn(Arrays.asList(service1, service2));

        when(appointmentRepository.findBarberPerformance(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(eq(barbershopId), any(), any()))
                .thenReturn(0L);

        // When
        AnalyticsResponse response = analyticsService.getAnalytics(barbershopId, "week");

        // Then
        assertEquals(2, response.getPopularServices().size());
        assertEquals("Haircut", response.getPopularServices().get(0).getServiceName());
        assertEquals(5L, response.getPopularServices().get(0).getBookingCount());
    }

    @Test
    void getAnalytics_WithTodayPeriod_UsesCorrectDateRange() {
        // Given - setup minimal mocks
        when(appointmentRepository.countByBarbershopId(barbershopId)).thenReturn(0L);
        when(appointmentRepository.countByBarbershopIdAndStatus(eq(barbershopId), any())).thenReturn(0L);
        when(appointmentRepository.findCompletedAppointmentsWithPrice(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.findPopularServices(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.findBarberPerformance(barbershopId)).thenReturn(List.of());
        when(appointmentRepository.countByBarbershopIdAndAppointmentDateTimeBetween(eq(barbershopId), any(), any()))
                .thenReturn(10L);

        // When
        AnalyticsResponse response = analyticsService.getAnalytics(barbershopId, "today");

        // Then
        assertNotNull(response);
        assertEquals(10L, response.getTodayAppointments());
    }
}
