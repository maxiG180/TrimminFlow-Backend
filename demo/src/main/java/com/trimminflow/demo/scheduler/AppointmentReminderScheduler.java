package com.trimminflow.demo.scheduler;

import com.trimminflow.demo.entity.Appointment;
import com.trimminflow.demo.entity.AppointmentStatus;
import com.trimminflow.demo.repository.AppointmentRepository;
import com.trimminflow.demo.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public AppointmentReminderScheduler(
            AppointmentRepository appointmentRepository,
            EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    /**
     * Runs every hour to send reminder emails for appointments 24 hours away
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void sendAppointmentReminders() {
        // Calculate time range: 23-25 hours from now (1 hour window)
        LocalDateTime start = LocalDateTime.now().plusHours(23);
        LocalDateTime end = LocalDateTime.now().plusHours(25);

        // Find appointments in the next 24 hours that haven't been sent reminders
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByBarbershopIdAndDateRange(null, start, end)
                .stream()
                .filter(apt -> !Boolean.TRUE.equals(apt.getReminderSent()))
                .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
                .filter(apt -> apt.getStatus() != AppointmentStatus.NO_SHOW)
                .filter(apt -> apt.getCustomerEmail() != null && !apt.getCustomerEmail().isEmpty())
                .filter(apt -> Boolean.TRUE.equals(apt.getBarbershop().getReminderEmailsEnabled())) // Check if
                                                                                                    // reminders are
                                                                                                    // enabled
                .toList();

        for (Appointment appointment : upcomingAppointments) {
            try {
                emailService.sendAppointmentReminder(appointment);
                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);

                System.out.println("Sent reminder for appointment: " + appointment.getId());
            } catch (Exception e) {
                System.err.println("Failed to send reminder for appointment " +
                        appointment.getId() + ": " + e.getMessage());
            }
        }

        if (!upcomingAppointments.isEmpty()) {
            System.out.println("Processed " + upcomingAppointments.size() + " appointment reminders");
        }
    }
}
