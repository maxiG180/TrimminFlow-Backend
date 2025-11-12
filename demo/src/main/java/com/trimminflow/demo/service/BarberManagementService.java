package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.BarberResponse;
import com.trimminflow.demo.dto.CreateBarberRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.UpdateBarberRequest;
import com.trimminflow.demo.entity.Barber;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarberRepository;
import com.trimminflow.demo.repository.BarbershopRepository;
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

        // Check if email already exists (if provided)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (barberRepository.existsByEmailAndBarbershopId(request.getEmail(), barbershopId)) {
                throw new RuntimeException("Email already exists for this barbershop");
            }
        }

        // Create barber
        Barber barber = new Barber(
                barbershop,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getBio()
        );

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

        List<BarberResponse> barberResponses = barberPage.getContent().stream()
                .map(BarberResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(
                barberResponses,
                barberPage.getNumber(),
                barberPage.getSize(),
                barberPage.getTotalElements(),
                barberPage.getTotalPages()
        );
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

        List<BarberResponse> barberResponses = barberPage.getContent().stream()
                .map(BarberResponse::new)
                .collect(Collectors.toList());

        return new PageResponse<>(
                barberResponses,
                barberPage.getNumber(),
                barberPage.getSize(),
                barberPage.getTotalElements(),
                barberPage.getTotalPages()
        );
    }

    /**
     * Get all active barbers for a barbershop (non-paginated - for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<BarberResponse> getActiveBarbers(UUID barbershopId) {
        List<Barber> barbers = barberRepository.findByBarbershopIdAndIsActiveTrue(barbershopId);
        return barbers.stream()
                .map(BarberResponse::new)
                .collect(Collectors.toList());
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

        // Update fields if provided
        if (request.getFirstName() != null) {
            barber.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            barber.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            barber.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            barber.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            barber.setBio(request.getBio());
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

        barber.setIsActive(false);
        barberRepository.save(barber);
    }

    /**
     * Permanently delete a barber
     */
    @Transactional
    public void hardDeleteBarber(UUID barberId, UUID barbershopId) {
        Barber barber = barberRepository.findByIdAndBarbershopId(barberId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        barberRepository.delete(barber);
    }
}
