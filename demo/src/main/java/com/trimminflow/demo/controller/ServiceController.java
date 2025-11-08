package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.service.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ServiceController
 *
 * REST API endpoints for managing barbershop services
 *
 * All endpoints require authentication (JWT token)
 * The barbershopId is extracted from the authenticated user's context
 */
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Service management APIs")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    public ServiceController(ServiceManagementService serviceManagementService) {
        this.serviceManagementService = serviceManagementService;
    }

    /**
     * Create a new service
     *
     * POST /api/v1/services
     *
     * Request Body:
     * {
     *   "name": "Haircut",
     *   "description": "Professional haircut",
     *   "price": 15.00,
     *   "durationMinutes": 30
     * }
     */
    @PostMapping
    @Operation(
        summary = "Create a new service",
        description = "Create a new service for the authenticated user's barbershop"
    )
    public ResponseEntity<ServiceResponse> createService(
        @Valid @RequestBody CreateServiceRequest request,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            ServiceResponse response = serviceManagementService.createService(barbershopId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all services for the barbershop
     *
     * GET /api/v1/services
     */
    @GetMapping
    @Operation(
        summary = "Get all services",
        description = "Get all services for the authenticated user's barbershop"
    )
    public ResponseEntity<List<ServiceResponse>> getAllServices(
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        List<ServiceResponse> services = serviceManagementService.getAllServices(barbershopId);
        return ResponseEntity.ok(services);
    }

    /**
     * Get all active services for the barbershop
     *
     * GET /api/v1/services/active
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active services",
        description = "Get all active services for the authenticated user's barbershop"
    )
    public ResponseEntity<List<ServiceResponse>> getActiveServices(
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        List<ServiceResponse> services = serviceManagementService.getActiveServices(barbershopId);
        return ResponseEntity.ok(services);
    }

    /**
     * Get a specific service by ID
     *
     * GET /api/v1/services/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get service by ID",
        description = "Get a specific service by its ID"
    )
    public ResponseEntity<ServiceResponse> getService(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            ServiceResponse service = serviceManagementService.getService(id, barbershopId);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a service
     *
     * PUT /api/v1/services/{id}
     *
     * Request Body (all fields optional):
     * {
     *   "name": "Updated Haircut",
     *   "price": 20.00,
     *   "durationMinutes": 45,
     *   "isActive": true
     * }
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a service",
        description = "Update an existing service. Only provided fields will be updated."
    )
    public ResponseEntity<ServiceResponse> updateService(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateServiceRequest request,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            ServiceResponse response = serviceManagementService.updateService(id, barbershopId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a service (soft delete)
     *
     * DELETE /api/v1/services/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a service",
        description = "Soft delete a service (sets isActive to false)"
    )
    public ResponseEntity<Void> deleteService(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            serviceManagementService.deleteService(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hard delete a service (permanent)
     *
     * DELETE /api/v1/services/{id}/hard
     */
    @DeleteMapping("/{id}/hard")
    @Operation(
        summary = "Permanently delete a service",
        description = "Hard delete a service (permanent deletion)"
    )
    public ResponseEntity<Void> hardDeleteService(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            serviceManagementService.hardDeleteService(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
