package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.Service;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ServiceManagementService
 *
 * Business logic for managing barbershop services
 * Handles CRUD operations for services
 */
@org.springframework.stereotype.Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;

    public ServiceManagementService(ServiceRepository serviceRepository,
                                   BarbershopRepository barbershopRepository) {
        this.serviceRepository = serviceRepository;
        this.barbershopRepository = barbershopRepository;
    }

    /**
     * Create a new service for a barbershop
     *
     * @param barbershopId The barbershop's UUID
     * @param request Service creation data
     * @return ServiceResponse with created service details
     */
    @Transactional
    public ServiceResponse createService(UUID barbershopId, CreateServiceRequest request) {
        // Find the barbershop
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
            .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        // Validate: Check for duplicate service name
        if (serviceRepository.existsByNameAndBarbershopId(request.getName(), barbershopId)) {
            throw new RuntimeException("A service with this name already exists");
        }

        // Validate: Price should be positive
        if (request.getPrice().doubleValue() < 0) {
            throw new RuntimeException("Price must be a positive value");
        }

        // Validate: Duration should be reasonable (between 5 and 480 minutes = 8 hours)
        if (request.getDurationMinutes() < 5 || request.getDurationMinutes() > 480) {
            throw new RuntimeException("Duration must be between 5 and 480 minutes");
        }

        // Create new service
        Service service = new Service(
            barbershop,
            request.getName().trim(),
            request.getDescription() != null ? request.getDescription().trim() : null,
            request.getPrice(),
            request.getDurationMinutes()
        );

        // Save to database
        Service savedService = serviceRepository.save(service);

        // Return response DTO
        return new ServiceResponse(savedService);
    }

    /**
     * Get all services for a barbershop (non-paginated for backward compatibility)
     *
     * @param barbershopId The barbershop's UUID
     * @return List of ServiceResponse
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices(UUID barbershopId) {
        List<Service> services = serviceRepository.findByBarbershopId(barbershopId);
        return services.stream()
            .map(ServiceResponse::new)
            .collect(Collectors.toList());
    }

    /**
     * Get all services for a barbershop with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PageResponse containing ServiceResponse list
     */
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getAllServicesPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Service> servicePage = serviceRepository.findByBarbershopId(barbershopId, pageable);

        List<ServiceResponse> content = servicePage.getContent().stream()
            .map(ServiceResponse::new)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            servicePage.getNumber(),
            servicePage.getSize(),
            servicePage.getTotalElements(),
            servicePage.getTotalPages()
        );
    }

    /**
     * Get all active services for a barbershop (non-paginated)
     *
     * @param barbershopId The barbershop's UUID
     * @return List of active ServiceResponse
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServices(UUID barbershopId) {
        List<Service> services = serviceRepository.findByBarbershopIdAndIsActive(barbershopId, true);
        return services.stream()
            .map(ServiceResponse::new)
            .collect(Collectors.toList());
    }

    /**
     * Get all active services for a barbershop with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PageResponse containing active ServiceResponse list
     */
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getActiveServicesPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Service> servicePage = serviceRepository.findByBarbershopIdAndIsActive(barbershopId, true, pageable);

        List<ServiceResponse> content = servicePage.getContent().stream()
            .map(ServiceResponse::new)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            servicePage.getNumber(),
            servicePage.getSize(),
            servicePage.getTotalElements(),
            servicePage.getTotalPages()
        );
    }

    /**
     * Search services by name or description with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param searchTerm The search term
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param activeOnly If true, only return active services
     * @return PageResponse containing matching ServiceResponse list
     */
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> searchServices(UUID barbershopId, String searchTerm, int page, int size, boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // If search term is empty, return all services
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return activeOnly ?
                getActiveServicesPaginated(barbershopId, page, size) :
                getAllServicesPaginated(barbershopId, page, size);
        }

        Page<Service> servicePage = activeOnly ?
            serviceRepository.searchActiveServices(barbershopId, searchTerm.trim(), pageable) :
            serviceRepository.searchServices(barbershopId, searchTerm.trim(), pageable);

        List<ServiceResponse> content = servicePage.getContent().stream()
            .map(ServiceResponse::new)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            servicePage.getNumber(),
            servicePage.getSize(),
            servicePage.getTotalElements(),
            servicePage.getTotalPages()
        );
    }

    /**
     * Get a specific service by ID
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     * @return ServiceResponse
     */
    @Transactional(readOnly = true)
    public ServiceResponse getService(UUID serviceId, UUID barbershopId) {
        // Use optimized query that checks both ID and barbershop in one query
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
            .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        return new ServiceResponse(service);
    }

    /**
     * Update an existing service
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     * @param request Update data (only provided fields will be updated)
     * @return ServiceResponse with updated service details
     */
    @Transactional
    public ServiceResponse updateService(UUID serviceId, UUID barbershopId, UpdateServiceRequest request) {
        // Find the service with authorization check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
            .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // Update only provided fields with validation
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            // Check for duplicate name (excluding current service)
            if (serviceRepository.existsByNameAndBarbershopIdExcludingId(trimmedName, barbershopId, serviceId)) {
                throw new RuntimeException("A service with this name already exists");
            }
            service.setName(trimmedName);
        }

        if (request.getDescription() != null) {
            service.setDescription(request.getDescription().trim());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().doubleValue() < 0) {
                throw new RuntimeException("Price must be a positive value");
            }
            service.setPrice(request.getPrice());
        }

        if (request.getDurationMinutes() != null) {
            if (request.getDurationMinutes() < 5 || request.getDurationMinutes() > 480) {
                throw new RuntimeException("Duration must be between 5 and 480 minutes");
            }
            service.setDurationMinutes(request.getDurationMinutes());
        }

        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }

        // Save updated service
        Service updatedService = serviceRepository.save(service);

        return new ServiceResponse(updatedService);
    }

    /**
     * Delete a service (soft delete by setting isActive = false)
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     */
    @Transactional
    public void deleteService(UUID serviceId, UUID barbershopId) {
        // Find the service with authorization check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
            .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // Soft delete (set isActive to false)
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    /**
     * Hard delete a service (permanent deletion)
     * Note: This should check for existing appointments before deletion
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     */
    @Transactional
    public void hardDeleteService(UUID serviceId, UUID barbershopId) {
        // Find the service with authorization check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
            .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // TODO: Add check for existing appointments using this service
        // If appointments exist, throw exception or prevent deletion

        // Hard delete (permanent)
        serviceRepository.delete(service);
    }
}
