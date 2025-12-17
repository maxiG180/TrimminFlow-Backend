package com.trimminflow.demo.service;

import com.resend.*;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.*;
import com.trimminflow.demo.entity.Appointment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailService(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email:onboarding@resend.dev}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    public void sendBookingConfirmation(Appointment appointment) {
        if (appointment.getCustomerEmail() == null || appointment.getCustomerEmail().isEmpty()) {
            return; // Skip if no email
        }

        try {
            String subject = "Appointment Confirmed - " + appointment.getBarbershop().getName();
            String htmlBody = buildConfirmationEmail(appointment);

            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(appointment.getCustomerEmail())
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(emailOptions);
        } catch (ResendException e) {
            // Log error but don't fail the appointment creation
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    public void sendAppointmentReminder(Appointment appointment) {
        if (appointment.getCustomerEmail() == null || appointment.getCustomerEmail().isEmpty()) {
            return;
        }

        try {
            String subject = "Reminder: Appointment Tomorrow at " + appointment.getBarbershop().getName();
            String htmlBody = buildReminderEmail(appointment);

            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(appointment.getCustomerEmail())
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(emailOptions);
        } catch (ResendException e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
        }
    }

    private String buildConfirmationEmail(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String appointmentTime = appointment.getAppointmentDateTime().format(formatter);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #FCD34D 0%%, #F59E0B 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .header h1 { color: #000; margin: 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .detail-box { background: white; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #F59E0B; }
                        .detail-row { margin: 10px 0; }
                        .label { font-weight: bold; color: #666; }
                        .value { color: #000; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✂️ Appointment Confirmed!</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>Your appointment at <strong>%s</strong> has been confirmed!</p>

                            <div class="detail-box">
                                <div class="detail-row">
                                    <span class="label">📅 Date & Time:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">💇 Service:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">✂️ Barber:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">💰 Price:</span>
                                    <span class="value">€%.2f</span>
                                </div>
                                %s
                            </div>

                            <p>We look forward to seeing you!</p>
                            <p><em>If you need to reschedule or cancel, please contact us as soon as possible.</em></p>
                        </div>
                        <div class="footer">
                            <p>This is an automated message from %s</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appointment.getCustomerName(),
                        appointment.getBarbershop().getName(),
                        appointmentTime,
                        appointment.getService().getName(),
                        appointment.getBarber().getFirstName() + " " + appointment.getBarber().getLastName(),
                        appointment.getService().getPrice(),
                        appointment.getBarbershop().getAddress() != null
                                ? "<div class=\"detail-row\"><span class=\"label\">📍 Location:</span><span class=\"value\">"
                                        + appointment.getBarbershop().getAddress() + "</span></div>"
                                : "",
                        appointment.getBarbershop().getName());
    }

    private String buildReminderEmail(Appointment appointment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        String appointmentTime = appointment.getAppointmentDateTime().format(formatter);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #3B82F6 0%%, #1D4ED8 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .header h1 { color: white; margin: 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .reminder-box { background: #EFF6FF; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #3B82F6; }
                        .detail-row { margin: 10px 0; }
                        .label { font-weight: bold; color: #666; }
                        .value { color: #000; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>⏰ Reminder: Appointment Tomorrow!</h1>
                        </div>
                        <div class="content">
                            <p>Hello <strong>%s</strong>,</p>
                            <p>This is a friendly reminder about your upcoming appointment at <strong>%s</strong>.</p>

                            <div class="reminder-box">
                                <div class="detail-row">
                                    <span class="label">📅 Date & Time:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">💇 Service:</span>
                                    <span class="value">%s</span>
                                </div>
                                <div class="detail-row">
                                    <span class="label">✂️ Barber:</span>
                                    <span class="value">%s</span>
                                </div>
                                %s
                            </div>

                            <p><strong>We're looking forward to seeing you tomorrow!</strong></p>
                            <p><em>If you need to cancel or reschedule, please let us know as soon as possible.</em></p>
                        </div>
                        <div class="footer">
                            <p>This is an automated reminder from %s</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(
                        appointment.getCustomerName(),
                        appointment.getBarbershop().getName(),
                        appointmentTime,
                        appointment.getService().getName(),
                        appointment.getBarber().getFirstName() + " " + appointment.getBarber().getLastName(),
                        appointment.getBarbershop().getAddress() != null
                                ? "<div class=\"detail-row\"><span class=\"label\">📍 Location:</span><span class=\"value\">"
                                        + appointment.getBarbershop().getAddress() + "</span></div>"
                                : "",
                        appointment.getBarbershop().getName());
    }
}
