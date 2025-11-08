package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.Service;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.ServiceRepository;
import org.springframework.stereotype.Service;
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

        // Create new service
        Service service = new Service(
            barbershop,
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getDurationMinutes()
        );

        // Save to database
        Service savedService = serviceRepository.save(service);

        // Return response DTO
        return new ServiceResponse(savedService);
    }

    /**
     * Get all services for a barbershop
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
     * Get all active services for a barbershop
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
     * Get a specific service by ID
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     * @return ServiceResponse
     */
    @Transactional(readOnly = true)
    public ServiceResponse getService(UUID serviceId, UUID barbershopId) {
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        // Verify service belongs to the barbershop
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new RuntimeException("Service does not belong to this barbershop");
        }

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
        // Find the service
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        // Verify service belongs to the barbershop
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new RuntimeException("Service does not belong to this barbershop");
        }

        // Update only provided fields
        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getDurationMinutes() != null) {
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
        // Find the service
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        // Verify service belongs to the barbershop
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new RuntimeException("Service does not belong to this barbershop");
        }

        // Soft delete (set isActive to false)
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    /**
     * Hard delete a service (permanent deletion)
     *
     * @param serviceId The service's UUID
     * @param barbershopId The barbershop's UUID (for authorization)
     */
    @Transactional
    public void hardDeleteService(UUID serviceId, UUID barbershopId) {
        // Find the service
        Service service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service not found"));

        // Verify service belongs to the barbershop
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new RuntimeException("Service does not belong to this barbershop");
        }

        // Hard delete (permanent)
        serviceRepository.delete(service);
    }
}
