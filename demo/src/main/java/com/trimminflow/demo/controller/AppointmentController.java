package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.dto.UpdateAppointmentRequest;
import com.trimminflow.demo.entity.AppointmentStatus;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// appointment management endpoints
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Appointment management APIs")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Unauthorized");
        }
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    @Operation(summary = "Create a new appointment", description = "Create a new appointment (Public endpoint)")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestHeader(value = "X-Barbershop-Id", required = true) UUID barbershopId,
            @Valid @RequestBody CreateAppointmentRequest request) {

        AppointmentResponse response = appointmentService.createAppointment(barbershopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get appointments", description = "Get paginated appointments (Protected)")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @RequestParam(required = false) UUID barberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDateTime").ascending());
        Page<AppointmentResponse> appointments = appointmentService.getAppointments(
                barbershopId, barberId, startDate, endDate, status, pageable);

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Get a specific appointment by its ID (Protected)")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        AppointmentResponse response = appointmentService.getAppointmentById(barbershopId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update appointment", description = "Update an existing appointment (Protected)")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {

        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        AppointmentResponse response = appointmentService.updateAppointment(barbershopId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment (Protected)")
    public ResponseEntity<Void> cancelAppointment(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        appointmentService.cancelAppointment(barbershopId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    @Operation(summary = "Get available time slots", description = "Get available time slots for a barber (Public)")
    public ResponseEntity<List<LocalDateTime>> getAvailableTimeSlots(
            @RequestParam UUID barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer serviceDuration) {

        List<LocalDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                barberId, date, serviceDuration);

        return ResponseEntity.ok(availableSlots);
    }
}
