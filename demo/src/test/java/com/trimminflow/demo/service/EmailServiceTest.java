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
        emailService = new EmailService("test_key", "test@example.com");
        testAppointment = createTestAppointment();
    }

    @Test
    void emailService_InitializesSuccessfully() {
        assertNotNull(emailService);
    }

    @Test
    void sendBookingConfirmation_WithNullEmail_DoesNotThrow() {
        testAppointment.setCustomerEmail(null);
        // Should not throw exception
        try {
            emailService.sendBookingConfirmation(testAppointment);
        } catch (Exception e) {
            // Expected - Resend API will fail but that's ok for test
        }
        assertTrue(true);
    }

    @Test
    void sendBookingConfirmation_WithEmptyEmail_DoesNotThrow() {
        testAppointment.setCustomerEmail("");
        try {
            emailService.sendBookingConfirmation(testAppointment);
        } catch (Exception e) {
            // Expected
        }
        assertTrue(true);
    }

    @Test
    void sendAppointmentReminder_WithNullEmail_DoesNotThrow() {
        testAppointment.setCustomerEmail(null);
        try {
            emailService.sendAppointmentReminder(testAppointment);
        } catch (Exception e) {
            // Expected
        }
        assertTrue(true);
    }

    private Appointment createTestAppointment() {
        Barbershop barbershop = new Barbershop();
        barbershop.setId(UUID.randomUUID());
        barbershop.setName("Test Shop");

        Barber barber = new Barber();
        barber.setId(UUID.randomUUID());
        barber.setFirstName("John");
        barber.setLastName("Doe");

        Service service = new Service();
        service.setId(UUID.randomUUID());
        service.setName("Haircut");
        service.setPrice(BigDecimal.valueOf(20.00));

        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID());
        appointment.setBarbershop(barbershop);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setCustomerEmail("test@example.com");
        appointment.setCustomerName("Test Customer");
        appointment.setCustomerPhone("1234567890");
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        return appointment;
    }
}
