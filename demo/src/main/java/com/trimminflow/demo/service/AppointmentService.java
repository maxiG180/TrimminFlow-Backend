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

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final CustomerRepository customerRepository;

    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public AppointmentService(AppointmentRepository appointmentRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository,
            BarbershopRepository barbershopRepository,
            BusinessHoursRepository businessHoursRepository,
            CustomerRepository customerRepository,
            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.barbershopRepository = barbershopRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.customerRepository = customerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public AppointmentResponse createAppointment(UUID barbershopId, CreateAppointmentRequest request) {
        Barber barber = barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        if (!barber.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Barber does not belong to this barbershop");
        }

        com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Service does not belong to this barbershop");
        }

        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new IllegalArgumentException("Barbershop not found"));

        validateAppointmentTime(request.getAppointmentDateTime());

        validateBusinessHours(barbershopId, request.getAppointmentDateTime(), service.getDurationMinutes());

        LocalDateTime endTime = request.getAppointmentDateTime().plusMinutes(service.getDurationMinutes());
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                request.getBarberId(),
                request.getAppointmentDateTime(),
                endTime);

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("This time slot is already booked");
        }

        Appointment appointment = new Appointment(
                barbershop,
                barber,
                service,
                request.getAppointmentDateTime(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone());
        appointment.setNotes(request.getNotes());

        // Find or Create Customer
        Customer customer = customerRepository.findByBarbershopIdAndPhone(barbershopId, request.getCustomerPhone())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(
                            barbershop,
                            request.getCustomerName(), // firstName (assuming full name for now or split if needed)
                            null, // lastName
                            request.getCustomerEmail(),
                            request.getCustomerPhone());
                    return customerRepository.save(newCustomer);
                });

        appointment.setCustomer(customer);

        appointment = appointmentRepository.save(appointment);
        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);
        // send update to everyone connected
        messagingTemplate.convertAndSend("/topic/appointments", response);
        return response;
    }

    public AppointmentResponse getAppointmentById(UUID barbershopId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    public Page<AppointmentResponse> getAppointments(UUID barbershopId,
            UUID barberId,
            LocalDate startDate,
            LocalDate endDate,
            AppointmentStatus status,
            Pageable pageable) {
        Page<Appointment> appointments;

        if (barberId != null && startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay();
            List<Appointment> list = appointmentRepository.findByBarberIdAndDateRange(barberId, start, end);
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

    @Transactional
    public AppointmentResponse updateAppointment(UUID barbershopId, UUID appointmentId,
            UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        // Ensure appointment has a customer (backfill for old appointments)
        if (appointment.getCustomer() == null && appointment.getCustomerPhone() != null) {
            // Extract to final variables for lambda
            final Barbershop barbershop = appointment.getBarbershop();
            final String customerName = appointment.getCustomerName();
            final String customerEmail = appointment.getCustomerEmail();
            final String customerPhone = appointment.getCustomerPhone();

            Customer customer = customerRepository
                    .findByBarbershopIdAndPhone(barbershopId, customerPhone)
                    .orElseGet(() -> {
                        Customer newCustomer = new Customer(
                                barbershop,
                                customerName,
                                null,
                                customerEmail,
                                customerPhone);
                        return customerRepository.save(newCustomer);
                    });
            appointment.setCustomer(customer);
        }

        if (request.getServiceId() != null) {
            com.trimminflow.demo.entity.Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new IllegalArgumentException("Service not found"));
            appointment.setService(service);
        }

        if (request.getAppointmentDateTime() != null) {
            validateAppointmentTime(request.getAppointmentDateTime());

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

        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getCustomerPhone() != null) {
            appointment.setCustomerPhone(request.getCustomerPhone());
        }

        appointment = appointmentRepository.save(appointment);
        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);
        // send update to everyone connected
        messagingTemplate.convertAndSend("/topic/appointments", response);
        return response;
    }

    @Transactional
    public void cancelAppointment(UUID barbershopId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getBarbershop().getId().equals(barbershopId)) {
            throw new IllegalArgumentException("Appointment does not belong to this barbershop");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        final Appointment savedAppointment = appointmentRepository.save(appointment);
        // send update to everyone connected
        messagingTemplate.convertAndSend("/topic/appointments", AppointmentResponse.fromEntity(savedAppointment));
    }

    public List<LocalDateTime> getAvailableTimeSlots(UUID barberId, LocalDate date, Integer serviceDuration) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        DayOfWeek dayOfWeek = convertToDayOfWeek(date.getDayOfWeek());
        BusinessHours businessHours = businessHoursRepository
                .findByBarbershopIdAndDayOfWeek(barber.getBarbershop().getId(), dayOfWeek)
                .orElse(null);

        if (businessHours == null || !businessHours.getIsOpen()) {
            return new ArrayList<>(); // shop is closed
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Appointment> existingAppointments = appointmentRepository
                .findByBarberIdAndDateRange(barberId, dayStart, dayEnd);

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = businessHours.getOpenTime();
        LocalTime closeTime = businessHours.getCloseTime();

        while (currentTime.plusMinutes(serviceDuration).isBefore(closeTime) ||
                currentTime.plusMinutes(serviceDuration).equals(closeTime)) {

            LocalDateTime slotDateTime = LocalDateTime.of(date, currentTime);
            LocalDateTime slotEndTime = slotDateTime.plusMinutes(serviceDuration);

            if (slotDateTime.isBefore(LocalDateTime.now())) {
                currentTime = currentTime.plusMinutes(15);
                continue;
            }

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

    private void validateAppointmentTime(LocalDateTime appointmentDateTime) {
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be in the future");
        }
    }

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
