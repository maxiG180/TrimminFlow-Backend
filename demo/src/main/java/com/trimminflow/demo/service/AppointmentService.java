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

// service layer for appointment management
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

    // create new appointment
    @Transactional
    public AppointmentResponse createAppointment(UUID barbershopId, CreateAppointmentRequest request) {
        // validate barber exists and belongs to barbershop
        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        if (!barber.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Barber does not belong to this barbershop");
        }

        // validate service exists and belongs to barbershop
        com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Service does not belong to this barbershop");
        }

        // get barbershop
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new IllegalArgumentException("Barbershop not found"));

        // validate appointment time
        validateAppointmentTime(request.getAppointmentDateTime());

        // validate business hours
        validateBusinessHours(barbershopId, request.getAppointmentDateTime(), service.getDurationMinutes());

        // check for conflicts
        LocalDateTime endTime = request.getAppointmentDateTime().plusMinutes(service.getDurationMinutes());
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.getBarberId(),
                request.getAppointmentDateTime(),
                endTime);

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("This time slot is already booked");
        }

        // create appointment
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

    // get appointment by id
    public AppointmentResponse getAppointmentById(UUID barbershopId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    // get appointments with filters
    public Page<AppointmentResponse> getAppointments(UUID barbershopId,
            UUID barberId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status,
            Pageable pageable) {
        Page<Appointment> appointments;

        if (barberId != null && startDate != null && endDate != null) {
            // filter by barber and date range
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            List<Appointment> list = appointmentRepository.findByBarberIdAndDateRange(barberId, start, end);
            // convert to page
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

    // update existing appointment
    @Transactional
    public AppointmentResponse updateAppointment(UUID barbershopId, UUID appointmentId,
            UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        // update service if provided
        if (request.getServiceId() != null) {
            com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service not found"));
            appointment.setService(service);
        }

        // update appointment time if provided
        if (request.getAppointmentDateTime() != null) {
            validateAppointmentTime(request.getAppointmentDateTime());

            // check for conflicts
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

        // update status if provided
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        // update notes if provided
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        // update customer phone if provided
        if (request.getCustomerPhone() != null) {
            appointment.setCustomerPhone(request.getCustomerPhone());
        }

        appointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(appointment);
    }

    // cancel appointment (soft delete)
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

    // get available time slots
    public List<LocalDateTime> getAvailableTimeSlots(UUID barberId, LocalDate date, Integer serviceDuration) {
        // get barber
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        // get business hours for this day
        DayOfWeek dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());
        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barber.getBarbershop().getId(), dayOfWeek)
                .orElse(null);

        if (businessHours == null || !businessHours.getIsOpen()) {
            return new ArrayList<>(); // shop is closed
        }

        // get existing appointments for this barber on this date
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Appointment> existingAppointments = appointmentRepository
                .findByBarberIdAndDateRange(barberId, dayStart, dayEnd);

        // generate time slots (15-minute intervals)
        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = businessHours.getOpenTime();
        LocalTime closeTime = businessHours.getCloseTime();

        while (currentTime.plusMinutes(serviceDuration).isBefore(closeTime) ||
                currentTime.plusMinutes(serviceDuration).equals(closeTime)) {

            LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEndTime = slotDateTime.plusMinutes(serviceDuration);

            // check if slot is in the past
            if (slotDateTime.isBefore(LocalDateTime.now())) {
                currentTime = currentTime.plusMinutes(15);
                continue;
            }

            // check if slot conflicts with existing appointments
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

    // validate appointment time is in the future
    private void validateAppointmentTime(LocalDateTime appointmentDateTime) {
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be in the future");
        }
    }

    // validate appointment is within business hours
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

    // convert java.time.DayOfWeek to entity DayOfWeek
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
