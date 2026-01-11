package com.trimminflow.demo.scheduler;

import com.trimminflow.demo.entity.*;
import com.trimminflow.demo.repository.AppointmentRepository;
import com.trimminflow.demo.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentReminderScheduler scheduler;

    private Barbershop barbershop;
    private Barber barber;
    private Service service;

    @BeforeEach
    void setUp() {
        barbershop = new Barbershop();
        barbershop.setId(UUID.randomUUID());
        barbershop.setName("Test Barbershop");
        barbershop.setReminderEmailsEnabled(true);

        barber = new Barber();
        barber.setId(UUID.randomUUID());
        barber.setFirstName("John");
        barber.setLastName("Doe");

        service = new Service();
        service.setId(UUID.randomUUID());
        service.setName("Haircut");
        service.setPrice(BigDecimal.valueOf(20.00));
        service.setDurationMinutes(30);
    }

    @Test
    void sendAppointmentReminders_WithEligibleAppointments_SendsEmails() {
        // Given
        Appointment appointment1 = createAppointment(true, AppointmentStatus.CONFIRMED, "test1@example.com");
        Appointment appointment2 = createAppointment(true, AppointmentStatus.CONFIRMED, "test2@example.com");

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(appointment1, appointment2));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, times(2)).sendAppointmentReminder(any(Appointment.class));
        verify(appointmentRepository, times(2)).save(any(Appointment.class));
    }

    @Test
    void sendAppointmentReminders_WithReminderAlreadySent_SkipsEmail() {
        // Given
        Appointment appointment = createAppointment(true, AppointmentStatus.CONFIRMED, "test@example.com");
        appointment.setReminderSent(true); // Already sent

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(appointment));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithCancelledAppointment_SkipsEmail() {
        // Given
        Appointment appointment = createAppointment(true, AppointmentStatus.CANCELLED, "test@example.com");

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(appointment));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithNoShowAppointment_SkipsEmail() {
        // Given
        Appointment appointment = createAppointment(true, AppointmentStatus.NO_SHOW, "test@example.com");

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(appointment));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithNoEmail_SkipsEmail() {
        // Given
        Appointment appointment = createAppointment(true, AppointmentStatus.CONFIRMED, null);

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(appointment));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithRemindersDisabled_SkipsEmail() {
        // Given
        barbershop.setReminderEmailsEnabled(false);
        Appointment appointment = createAppointment(false, AppointmentStatus.CONFIRMED, "test@example.com");

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of(appointment));

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithNoAppointments_DoesNothing() {
        // Given
        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of());

        // When
        scheduler.sendAppointmentReminders();

        // Then
        verify(emailService, never()).sendAppointmentReminder(any());
    }

    @Test
    void sendAppointmentReminders_WithEmailFailure_ContinuesProcessing() {
        // Given
        Appointment appointment1 = createAppointment(true, AppointmentStatus.CONFIRMED, "test1@example.com");
        Appointment appointment2 = createAppointment(true, AppointmentStatus.CONFIRMED, "test2@example.com");

        when(appointmentRepository.findByBarbershopIdAndDateRange(any(), any(), any()))
                .thenReturn(Arrays.asList(appointment1, appointment2));
        doThrow(new RuntimeException("Email failed")).when(emailService).sendAppointmentReminder(appointment1);

        // When
        scheduler.sendAppointmentReminders();

        // Then - should still try to send to appointment2
        verify(emailService, times(2)).sendAppointmentReminder(any());
    }

    private Appointment createAppointment(boolean remindersEnabled, AppointmentStatus status, String email) {
        Barbershop shop = new Barbershop();
        shop.setId(UUID.randomUUID());
        shop.setName("Test Shop");
        shop.setReminderEmailsEnabled(remindersEnabled);

        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID());
        appointment.setBarbershop(shop);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setStatus(status);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setCustomerEmail(email);
        appointment.setCustomerName("Test Customer");
        appointment.setCustomerPhone("1234567890");
        appointment.setReminderSent(false);
        return appointment;
    }
}
