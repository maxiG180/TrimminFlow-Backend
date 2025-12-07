package com.trimminflow.demo.controller;

import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.ErrorResponse;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.UpdateBarberRequest;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.service.BarberManagementService;
import com.trimminflow.demo.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

// barber management endpoints
@RestController
@RequestMapping("/api/v1/barbers")
@Tag(name = "Barbers", description = "Barber management APIs")
public class BarberController {

    private final BarberManagementService barberManagementService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    public BarberController(BarberManagementService barberManagementService,
            CloudinaryService cloudinaryService,
            UserRepository userRepository) {
        this.barberManagementService = barberManagementService;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Create a new barber", description = "Create a new barber for the authenticated user's barbershop")
    public ResponseEntity<?> createBarber(
            @Valid @ModelAttribute CreateBarberRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                request.setProfileImageUrl(imageUrl);
            }

            BarberResponse response = barberManagementService.createBarber(barbershopId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Failed to create barber", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get barbers with pagination", description = "Get paginated barbers with optional search by name or email")
    public ResponseEntity<PageResponse<BarberResponse>> getAllBarbers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        PageResponse<BarberResponse> response;

        if (search != null && !search.trim().isEmpty()) {
            response = barberManagementService.searchBarbers(barbershopId, search, page, size, activeOnly);
        } else {
            response = activeOnly ? barberManagementService.getActiveBarbersPaginated(barbershopId, page, size)
                    : barberManagementService.getAllBarbersPaginated(barbershopId, page, size);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all barbers (non-paginated)", description = "Get all barbers for the authenticated user's barbershop without pagination")
    public ResponseEntity<List<BarberResponse>> getAllBarbersNonPaginated() {
        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        List<BarberResponse> barbers = barberManagementService.getAllBarbers(barbershopId);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active barbers (paginated)", description = "Get paginated active barbers for the authenticated user's barbershop")
    public ResponseEntity<PageResponse<BarberResponse>> getActiveBarbers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = getAuthenticatedUser();
        UUID barbershopId = user.getBarbershop().getId();

        PageResponse<BarberResponse> barbers = barberManagementService.getActiveBarbersPaginated(barbershopId, page,
                size);
        return ResponseEntity.ok(barbers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get barber by ID", description = "Get a specific barber by its ID")
    public ResponseEntity<?> getBarber(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            BarberResponse barber = barberManagementService.getBarber(id, barbershopId);
            return ResponseEntity.ok(barber);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Update a barber", description = "Update an existing barber. Only provided fields will be updated.")
    public ResponseEntity<?> updateBarber(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateBarberRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                request.setProfileImageUrl(imageUrl);
            }

            BarberResponse response = barberManagementService.updateBarber(id, barbershopId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Failed to update barber", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a barber", description = "Soft delete a barber (sets isActive to false)")
    public ResponseEntity<?> deleteBarber(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            barberManagementService.deleteBarber(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Permanently delete a barber", description = "Hard delete a barber (permanent deletion)")
    public ResponseEntity<?> hardDeleteBarber(@PathVariable UUID id) {
        try {
            User user = getAuthenticatedUser();
            UUID barbershopId = user.getBarbershop().getId();

            barberManagementService.hardDeleteBarber(id, barbershopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Barber not found", e.getMessage()));
        }
    }
}
