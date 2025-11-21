package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.UpdateBarberRequest;
import com.trimminflow.demo.entity.Barber;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarberRepository;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BarberManagementService
 *
 * Business logic for managing barbers
 */
@Service
public class BarberManagementService {

    private final BarberRepository barberRepository;
    private final BarbershopRepository barbershopRepository;

    public BarberManagementService(BarberRepository barberRepository, BarbershopRepository barbershopRepository) {
        this.barberRepository = barberRepository;
        this.barbershopRepository = barbershopRepository;
    }

    /**
     * Create a new barber
     */
    @Transactional
    public BarberResponse createBarber(UUID barbershopId, CreateBarberRequest request) {
        // Validate barbershop exists
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        // Validate: Trim and check names
        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new RuntimeException("First name and last name cannot be empty");
        }

        // Check if email already exists (if provided)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String trimmedEmail = request.getEmail().trim();
            if (barberRepository.existsByEmailAndBarbershopId(trimmedEmail, barbershopId)) {
                throw new RuntimeException("Email already exists for this barbershop");
            }
        }

        // Create barber
        Barber barber = new Barber(
                barbershop,
                firstName,
                lastName,
                request.getEmail() != null ? request.getEmail().trim() : null,
                request.getPhone() != null ? request.getPhone().trim() : null,
                request.getBio() != null ? request.getBio().trim() : null);

        // Set profile image URL if provided
        if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().trim().isEmpty()) {
            barber.setProfileImageUrl(request.getProfileImageUrl().trim());
        }

        Barber savedBarber = barberRepository.save(barber);
        return new BarberResponse(savedBarber);
    }

    /**
     * Get all barbers for a barbershop (paginated)
     */
    @Transactional(readOnly = true)
    public PageResponse<BarberResponse> getAllBarbersPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Barber> barberPage = barberRepository.findByBarbershopId(barbershopId, pageable);

        return PaginationUtils.createPageResponse(barberPage, BarberResponse::new);
    }

    /**
     * Get all barbers for a barbershop (non-paginated - for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<BarberResponse> getAllBarbers(UUID barbershopId) {
        List<Barber> barbers = barberRepository.findByBarbershopId(barbershopId);
        return barbers.stream()
                .map(BarberResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get all active barbers for a barbershop (paginated)
     */
    @Transactional(readOnly = true)
    public PageResponse<BarberResponse> getActiveBarbersPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Barber> barberPage = barberRepository.findByBarbershopIdAndIsActiveTrue(barbershopId, pageable);

        return PaginationUtils.createPageResponse(barberPage, BarberResponse::new);
    }

    /**
     * Get all active barbers for a barbershop (non-paginated - for backward
     * compatibility)
     */
    @Transactional(readOnly = true)
    public List<BarberResponse> getActiveBarbers(UUID barbershopId) {
        List<Barber> barbers = barberRepository.findByBarbershopIdAndIsActiveTrue(barbershopId);
        return barbers.stream()
                .map(BarberResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Search barbers by name or email with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param searchTerm   The search term
     * @param page         Page number (0-indexed)
     * @param size         Page size
     * @param activeOnly   If true, only return active barbers
     * @return PageResponse containing matching BarberResponse list
     */
    @Transactional(readOnly = true)
    public PageResponse<BarberResponse> searchBarbers(UUID barbershopId, String searchTerm, int page, int size,
            boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // If search term is empty, return all barbers
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return activeOnly ? getActiveBarbersPaginated(barbershopId, page, size)
                    : getAllBarbersPaginated(barbershopId, page, size);
        }

        Page<Barber> barberPage = activeOnly
                ? barberRepository.searchActiveBarbers(barbershopId, searchTerm.trim(), pageable)
                : barberRepository.searchBarbers(barbershopId, searchTerm.trim(), pageable);

        return PaginationUtils.createPageResponse(barberPage, BarberResponse::new);
    }

    /**
     * Get a specific barber
     */
    @Transactional(readOnly = true)
    public BarberResponse getBarber(UUID barberId, UUID barbershopId) {
        Barber barber = barberRepository.findByIdAndBarbershopId(barberId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        return new BarberResponse(barber);
    }

    /**
     * Update a barber
     */
    @Transactional
    public BarberResponse updateBarber(UUID barberId, UUID barbershopId, UpdateBarberRequest request) {
        Barber barber = barberRepository.findByIdAndBarbershopId(barberId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        // Update fields if provided with validation
        if (request.getFirstName() != null) {
            String trimmedFirstName = request.getFirstName().trim();
            if (trimmedFirstName.isEmpty()) {
                throw new RuntimeException("First name cannot be empty");
            }
            barber.setFirstName(trimmedFirstName);
        }

        if (request.getLastName() != null) {
            String trimmedLastName = request.getLastName().trim();
            if (trimmedLastName.isEmpty()) {
                throw new RuntimeException("Last name cannot be empty");
            }
            barber.setLastName(trimmedLastName);
        }

        if (request.getEmail() != null) {
            String trimmedEmail = request.getEmail().trim();
            // Check for duplicate email (excluding current barber)
            if (!trimmedEmail.isEmpty() &&
                    barberRepository.existsByEmailAndBarbershopIdExcludingId(trimmedEmail, barbershopId, barberId)) {
                throw new RuntimeException("Email already exists for another barber in this barbershop");
            }
            barber.setEmail(trimmedEmail.isEmpty() ? null : trimmedEmail);
        }

        if (request.getPhone() != null) {
            barber.setPhone(request.getPhone().trim());
        }

        if (request.getBio() != null) {
            barber.setBio(request.getBio().trim());
        }

        if (request.getProfileImageUrl() != null) {
            String trimmedUrl = request.getProfileImageUrl().trim();
            barber.setProfileImageUrl(trimmedUrl.isEmpty() ? null : trimmedUrl);
        }

        if (request.getIsActive() != null) {
            barber.setIsActive(request.getIsActive());
        }

        Barber updatedBarber = barberRepository.save(barber);
        return new BarberResponse(updatedBarber);
    }

    /**
     * Delete a barber (soft delete - set isActive to false)
     */
    @Transactional
    public void deleteBarber(UUID barberId, UUID barbershopId) {
        Barber barber = barberRepository.findByIdAndBarbershopId(barberId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        // TODO: Add check for existing appointments with this barber
        // If appointments exist, you may want to:
        // 1. Prevent deletion and throw exception
        // 2. Reassign appointments to another barber
        // 3. Cancel future appointments

        barber.setIsActive(false);
        barberRepository.save(barber);
    }

    /**
     * Permanently delete a barber
     * Note: This should check for existing appointments before deletion
     */
    @Transactional
    public void hardDeleteBarber(UUID barberId, UUID barbershopId) {
        Barber barber = barberRepository.findByIdAndBarbershopId(barberId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        // TODO: Add check for existing appointments with this barber
        // Hard deletion should probably be prevented if appointments exist
        // Consider soft delete instead

        barberRepository.delete(barber);
    }
}
