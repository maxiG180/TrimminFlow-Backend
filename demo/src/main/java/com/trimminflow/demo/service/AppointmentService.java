package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.dto.UpdateAppointmentRequest;
import com.trimminflow.demo.entity.*;
import com.trimminflow.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for Appointment management
 *
 * Handles business logic for creating, updating, and managing appointments
 * including conflict detection and availability calculation
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;
    private final BusinessHoursRepository businessHoursRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            BarbershopRepository barbershopRepository,
            BusinessHoursRepository businessHoursRepository) {
        this.appointmentRepository = appointmentRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.barbershopRepository = barbershopRepository;
        this.businessHoursRepository = businessHoursRepository;
    }

    /**
     * Create a new appointment
     */
    @Transactional
    public AppointmentResponse createAppointment(UUID barbershopId, CreateAppointmentRequest request) {
        // Validate barber exists and belongs to barbershop
        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        if (!barber.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Barber does not belong to this barbershop");
        }

        // Validate service exists and belongs to barbershop
        com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Service does not belong to this barbershop");
        }

        // Get barbershop
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new IllegalArgumentException("Barbershop not found"));

        // Validate appointment time
        validateAppointmentTime(request.getAppointmentDateTime());

        // Validate business hours
        validateBusinessHours(barbershopId, request.getAppointmentDateTime(), service.getDurationMinutes());

        // Check for conflicts
        LocalDateTime endTime = request.getAppointmentDateTime().plusMinutes(service.getDurationMinutes());
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.getBarberId(),
                request.getAppointmentDateTime(),
                endTime);

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("This time slot is already booked");
        }

        // Create appointment
        Appointment appointment = new Appointment(
                barbershop,
                barber,
                service,
                request.getAppointmentDateTime(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone());
        appointment.setNotes(request.getNotes());

        appointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Get appointment by ID
     */
    public AppointmentResponse getAppointmentById(UUID barbershopId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Get appointments for a barbershop with optional filters
     */
    public Page<AppointmentResponse> getAppointments(UUID barbershopId,
            UUID barberId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status,
            Pageable pageable) {
        Page<Appointment> appointments;

        if (barberId != null && startDate != null && endDate != null) {
            // Filter by barber and date range
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            List<Appointment> list = appointmentRepository.findByBarberIdAndDateRange(barberId, start, end);
            // Convert to page (simplified - in production use proper pagination)
            appointments = Page.empty(pageable);
        } else if (status != null) {
            appointments = appointmentRepository.findByBarbershopIdAndStatus(barbershopId, status, pageable);
        } else {
            appointments = appointmentRepository.findByBarbershopIdAndStatusNot(
                    barbershopId,
                    AppointmentStatus.CANCELLED,
                    pageable);
        }

        return appointments.map(AppointmentResponse::fromEntity);
    }

    /**
     * Update an existing appointment
     */
    @Transactional
    public AppointmentResponse updateAppointment(UUID barbershopId, UUID appointmentId,
            UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        // Update service if provided
        if (request.getServiceId() != null) {
            com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service not found"));
            appointment.setService(service);
        }

        // Update appointment time if provided
        if (request.getAppointmentDateTime() != null) {
            validateAppointmentTime(request.getAppointmentDateTime());

            // Check for conflicts (excluding current appointment)
            LocalDateTime endTime = request.getAppointmentDateTime()
                    .plusMinutes(appointment.getService().getDurationMinutes());

            List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                    appointment.getBarber().getId(),
                    request.getAppointmentDateTime(),
                    endTime);

            conflicts.removeIf(a -> a.getId().equals(appointmentId));

            if (!conflicts.isEmpty()) {
                throw new IllegalArgumentException("This time slot is already booked");
            }

            appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        }

        // Update status if provided
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        // Update notes if provided
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        // Update customer phone if provided
        if (request.getCustomerPhone() != null) {
            appointment.setCustomerPhone(request.getCustomerPhone());
        }

        appointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Cancel an appointment (soft delete)
     */
    @Transactional
    public void cancelAppointment(UUID barbershopId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    /**
     * Get available time slots for a barber on a specific date
     */
    public List<LocalDateTime> getAvailableTimeSlots(UUID barberId, LocalDate date, Integer serviceDuration) {
        // Get barber
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        // Get business hours for this day
        DayOfWeek dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());
        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barber.getBarbershop().getId(), dayOfWeek)
                .orElse(null);

        if (businessHours == null || !businessHours.getIsOpen()) {
            return new ArrayList<>(); // Shop is closed
        }

        // Get existing appointments for this barber on this date
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Appointment> existingAppointments = appointmentRepository
                .findByBarberIdAndDateRange(barberId, dayStart, dayEnd);

        // Generate time slots (15-minute intervals)
        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = businessHours.getOpenTime();
        LocalTime closeTime = businessHours.getCloseTime();

        while (currentTime.plusMinutes(serviceDuration).isBefore(closeTime) ||
                currentTime.plusMinutes(serviceDuration).equals(closeTime)) {

            LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEndTime = slotDateTime.plusMinutes(serviceDuration);

            // Check if slot is in the past
            if (slotDateTime.isBefore(LocalDateTime.now())) {
                currentTime = currentTime.plusMinutes(15);
                continue;
            }

            // Check if slot conflicts with existing appointments
            boolean hasConflict = false;
            for (Appointment apt : existingAppointments) {
                if (slotDateTime.isBefore(apt.getEndDateTime()) &&
                        slotEndTime.isAfter(apt.getAppointmentDateTime())) {
                    hasConflict = true;
                    break;
                }
            }

            if (!hasConflict) {
                availableSlots.add(slotDateTime);
            }

            currentTime = currentTime.plusMinutes(15);
        }

        return availableSlots;
    }

    /**
     * Validate appointment time is in the future
     */
    private void validateAppointmentTime(LocalDateTime appointmentDateTime) {
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be in the future");
        }
    }

    /**
     * Validate appointment is within business hours
     */
    private void validateBusinessHours(UUID barbershopId, LocalDateTime appointmentDateTime,
            Integer serviceDuration) {
        DayOfWeek dayOfWeek = convertToDayOfWeek(appointmentDateTime.getDayOfWeek());
        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barbershopId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("Business hours not set for this day"));

        if (!businessHours.getIsOpen()) {
            throw new IllegalArgumentException("Barbershop is closed on this day");
        }

        LocalTime appointmentTime = appointmentDateTime.toLocalTime();
        LocalTime endTime = appointmentTime.plusMinutes(serviceDuration);

        if (appointmentTime.isBefore(businessHours.getOpenTime()) ||
                endTime.isAfter(businessHours.getCloseTime())) {
            throw new IllegalArgumentException(
                    String.format("Appointment must be between %s and %s",
                            businessHours.getOpenTime(),
                            businessHours.getCloseTime()));
        }
    }

    /**
     * Convert java.time.DayOfWeek to entity DayOfWeek
     */
    private DayOfWeek convertToDayOfWeek(java.time.DayOfWeek javaDayOfWeek) {
        return switch (javaDayOfWeek) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }
}
