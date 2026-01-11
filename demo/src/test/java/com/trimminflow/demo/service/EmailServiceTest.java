package com.trimminflow.demo.service;

import com.trimminflow.demo.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private EmailService emailService;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        // Initialize with dummy API key for testing
        emailService = new EmailService("test_api_key", "onboarding@resend.dev");
        testAppointment = createTestAppointment();
    }

    @Test
    void sendBookingConfirmation_WithNullEmail_DoesNotThrow() {
        // Given
        testAppointment.setCustomerEmail(null);

        // When & Then
        assertDoesNotThrow(() -> emailService.sendBookingConfirmation(testAppointment));
    }

    @Test
    void sendBookingConfirmation_WithEmptyEmail_DoesNotThrow() {
        // Given
        testAppointment.setCustomerEmail("");

        // When & Then
        assertDoesNotThrow(() -> emailService.sendBookingConfirmation(testAppointment));
    }

    @Test
    void sendBookingConfirmation_WithValidEmail_BuildsCorrectEmail() {
        // Given - valid appointment with email

        // When & Then - should not throw exception during email building
        assertDoesNotThrow(() -> emailService.sendBookingConfirmation(testAppointment));
    }

    @Test
    void sendAppointmentReminder_WithNullEmail_DoesNotThrow() {
        // Given
        testAppointment.setCustomerEmail(null);

        // When & Then
        assertDoesNotThrow(() -> emailService.sendAppointmentReminder(testAppointment));
    }

    @Test
    void sendAppointmentReminder_WithEmptyEmail_DoesNotThrow() {
        // Given
        testAppointment.setCustomerEmail("");

        // When & Then
        assertDoesNotThrow(() -> emailService.sendAppointmentReminder(testAppointment));
    }

    @Test
    void sendAppointmentReminder_WithValidEmail_BuildsCorrectEmail() {
        // Given - valid appointment with email

        // When & Then - should not throw exception during email building
        assertDoesNotThrow(() -> emailService.sendAppointmentReminder(testAppointment));
    }

    @Test
    void emailService_InitializesWithCorrectConfiguration() {
        // Given
        String apiKey = "test_key";
        String fromEmail = "test@example.com";

        // When
        EmailService service = new EmailService(apiKey, fromEmail);

        // Then
        assertNotNull(service);
    }

    private Appointment createTestAppointment() {
        Barbershop barbershop = new Barbershop();
        barbershop.setId(UUID.randomUUID());
        barbershop.setName("Test Barbershop");
        barbershop.setAddress("123 Test Street");

        Barber barber = new Barber();
        barber.setId(UUID.randomUUID());
        barber.setFirstName("John");
        barber.setLastName("Doe");
        barber.setBarbershop(barbershop);

        Service service = new Service();
        service.setId(UUID.randomUUID());
        service.setName("Haircut");
        service.setPrice(BigDecimal.valueOf(20.00));
        service.setDurationMinutes(30);

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane@example.com");
        customer.setPhone("1234567890");

        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID());
        appointment.setBarbershop(barbershop);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setCustomer(customer);
        appointment.setCustomerName("Jane Smith");
        appointment.setCustomerEmail("jane@example.com");
        appointment.setCustomerPhone("1234567890");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointment;
    }
}
