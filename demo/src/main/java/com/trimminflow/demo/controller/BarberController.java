package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.ErrorResponse;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.UpdateBarberRequest;
import com.trimminflow.demo.service.BarberManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * BarberController
 *
 * REST API endpoints for managing barbers
 */
@RestController
@RequestMapping("/api/v1/barbers")
@Tag(name = "Barbers", description = "Barber management APIs")
public class BarberController {

    private final BarberManagementService barberManagementService;

    public BarberController(BarberManagementService barberManagementService) {
        this.barberManagementService = barberManagementService;
    }

    /**
     * Create a new barber
     *
     * POST /api/v1/barbers
     */
    @PostMapping
    @Operation(
        summary = "Create a new barber",
        description = "Create a new barber for the authenticated user's barbershop"
    )
    public ResponseEntity<?> createBarber(
        @Valid @RequestBody CreateBarberRequest request,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            BarberResponse response = barberManagementService.createBarber(barbershopId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create barber", e.getMessage()));
        }
    }

    /**
     * Get barbers with pagination and optional search
     *
     * GET /api/v1/barbers?page=0&size=10&search=john&activeOnly=true
     */
    @GetMapping
    @Operation(
        summary = "Get barbers with pagination",
        description = "Get paginated barbers with optional search by name or email"
    )
    public ResponseEntity<PageResponse<BarberResponse>> getAllBarbers(
        @RequestHeader("X-Barbershop-Id") UUID barbershopId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        PageResponse<BarberResponse> response;

        if (search != null && !search.trim().isEmpty()) {
            response = barberManagementService.searchBarbers(barbershopId, search, page, size, activeOnly);
        } else {
            response = activeOnly ?
                barberManagementService.getActiveBarbersPaginated(barbershopId, page, size) :
                barberManagementService.getAllBarbersPaginated(barbershopId, page, size);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all barbers for the barbershop (non-paginated)
     *
     * GET /api/v1/barbers/all
     */
    @GetMapping("/all")
    @Operation(
        summary = "Get all barbers (non-paginated)",
        description = "Get all barbers for the authenticated user's barbershop without pagination"
    )
    public ResponseEntity<List<BarberResponse>> getAllBarbersNonPaginated(
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        List<BarberResponse> barbers = barberManagementService.getAllBarbers(barbershopId);
        return ResponseEntity.ok(barbers);
    }

    /**
     * Get all active barbers for the barbershop (paginated)
     *
     * GET /api/v1/barbers/active?page=0&size=10
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active barbers (paginated)",
        description = "Get paginated active barbers for the authenticated user's barbershop"
    )
    public ResponseEntity<PageResponse<BarberResponse>> getActiveBarbers(
        @RequestHeader("X-Barbershop-Id") UUID barbershopId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<BarberResponse> barbers = barberManagementService.getActiveBarbersPaginated(barbershopId, page, size);
        return ResponseEntity.ok(barbers);
    }

    /**
     * Get a specific barber by ID
     *
     * GET /api/v1/barbers/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get barber by ID",
        description = "Get a specific barber by its ID"
    )
    public ResponseEntity<?> getBarber(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            BarberResponse barber = barberManagementService.getBarber(id, barbershopId);
            return ResponseEntity.ok(barber);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }

    /**
     * Update a barber
     *
     * PUT /api/v1/barbers/{id}
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a barber",
        description = "Update an existing barber. Only provided fields will be updated."
    )
    public ResponseEntity<?> updateBarber(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateBarberRequest request,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            BarberResponse response = barberManagementService.updateBarber(id, barbershopId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update barber", e.getMessage()));
        }
    }

    /**
     * Delete a barber (soft delete)
     *
     * DELETE /api/v1/barbers/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a barber",
        description = "Soft delete a barber (sets isActive to false)"
    )
    public ResponseEntity<?> deleteBarber(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            barberManagementService.deleteBarber(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }

    /**
     * Hard delete a barber (permanent)
     *
     * DELETE /api/v1/barbers/{id}/hard
     */
    @DeleteMapping("/{id}/hard")
    @Operation(
        summary = "Permanently delete a barber",
        description = "Hard delete a barber (permanent deletion)"
    )
    public ResponseEntity<?> hardDeleteBarber(
        @PathVariable UUID id,
        @RequestHeader("X-Barbershop-Id") UUID barbershopId
    ) {
        try {
            barberManagementService.hardDeleteBarber(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }
}
