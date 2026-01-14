package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ErrorResponse;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.ServiceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// service management endpoints
@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Service management APIs")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;
    private final UserRepository userRepository;

    public ServiceController(ServiceManagementService serviceManagementService, UserRepository userRepository) {
        this.serviceManagementService = serviceManagementService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    @Operation(summary = "Create a new service", description = "Create a new service for the authenticated user's barbershop")
    public ResponseEntity<?> createService(@Valid @RequestBody CreateServiceRequest request) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            ServiceResponse response = serviceManagementService.createService(barbershopId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Service creation failed", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all services", description = "Get all services for the authenticated user's barbershop (non-paginated)")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        List<ServiceResponse> services = serviceManagementService.getAllServices(barbershopId);
        return ResponseEntity.ok(services);
    }

    @GetMapping
    @Operation(summary = "Get services with pagination", description = "Get paginated services with optional search by name or description")
    public ResponseEntity<PageResponse<ServiceResponse>> getServicesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        PageResponse<ServiceResponse> response;

        if (search != null && !search.trim().isEmpty()) {
            response = serviceManagementService.searchServices(barbershopId, search, page, size, activeOnly);
        } else {
            response = activeOnly ? serviceManagementService.getActiveServicesPaginated(barbershopId, page, size)
                    : serviceManagementService.getAllServicesPaginated(barbershopId, page, size);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active services", description = "Get all active services for the authenticated user's barbershop")
    public ResponseEntity<List<ServiceResponse>> getActiveServices() {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        List<ServiceResponse> services = serviceManagementService.getActiveServices(barbershopId);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID", description = "Get a specific service by its ID")
    public ResponseEntity<ServiceResponse> getService(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            ServiceResponse service = serviceManagementService.getService(id, barbershopId);
            return ResponseEntity.ok(service);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service", description = "Update an existing service. Only provided fields will be updated.")
    public ResponseEntity<?> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            ServiceResponse response = serviceManagementService.updateService(id, barbershopId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Service update failed", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service", description = "Soft delete a service (sets isActive to false)")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            serviceManagementService.deleteService(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Permanently delete a service", description = "Hard delete a service (permanent deletion)")
    public ResponseEntity<Void> hardDeleteService(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            serviceManagementService.hardDeleteService(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
