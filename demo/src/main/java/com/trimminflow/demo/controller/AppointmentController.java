package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.AppointmentResponse;
import com.trimminflow.demo.dto.CreateAppointmentRequest;
import com.trimminflow.demo.dto.UpdateAppointmentRequest;
import com.trimminflow.demo.entity.AppointmentStatus;
import com.trimminflow.demo.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// appointment management endpoints
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId,
            @Valid @RequestBody CreateAppointmentRequest request) {

        AppointmentResponse response = appointmentService.createAppointment(barbershopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId,
            @RequestParam(required = false) UUID barberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDateTime").ascending());
        Page<AppointmentResponse> appointments = appointmentService.getAppointments(
                barbershopId, barberId, startDate, endDate, status, pageable);

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId,
            @PathVariable UUID id) {

        AppointmentResponse response = appointmentService.getAppointmentById(barbershopId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {

        AppointmentResponse response = appointmentService.updateAppointment(barbershopId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(
            @RequestHeader("X-Barbershop-Id") UUID barbershopId,
            @PathVariable UUID id) {

        appointmentService.cancelAppointment(barbershopId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<List<LocalDateTime>> getAvailableTimeSlots(
            @RequestParam UUID barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer serviceDuration) {

        List<LocalDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                barberId, date, serviceDuration);

        return ResponseEntity.ok(availableSlots);
    }
}
