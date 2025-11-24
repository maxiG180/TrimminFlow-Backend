package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.CreateServiceRequest;
import com.trimminflow.demo.dto.PageResponse;
import com.trimminflow.demo.dto.ServiceResponse;
import com.trimminflow.demo.dto.UpdateServiceRequest;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.Service;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.ServiceRepository;
import com.trimminflow.demo.util.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// service management service
@org.springframework.stereotype.Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final BarbershopRepository barbershopRepository;

    public ServiceManagementService(ServiceRepository serviceRepository,
            BarbershopRepository barbershopRepository) {
        this.serviceRepository = serviceRepository;
        this.barbershopRepository = barbershopRepository;
    }

    // create new service
    @Transactional
    public ServiceResponse createService(UUID barbershopId, CreateServiceRequest request) {
        // find barbershop
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        // check duplicate name
        if (serviceRepository.existsByNameAndBarbershopId(request.getName(), barbershopId)) {
            throw new RuntimeException("A service with this name already exists");
        }

        // validate price
        if (request.getPrice().doubleValue() < 0) {
            throw new RuntimeException("Price must be a positive value");
        }

        // validate duration
        if (request.getDurationMinutes() < 5 || request.getDurationMinutes() > 480) {
            throw new RuntimeException("Duration must be between 5 and 480 minutes");
        }

        // create service
        Service service = new Service(
                barbershop,
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getPrice(),
                request.getDurationMinutes());

        // save to database
        Service savedService = serviceRepository.save(service);

        // return response
        return new ServiceResponse(savedService);
    }

    // get all services non-paginated
    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices(UUID barbershopId) {
        List<Service> services = serviceRepository.findByBarbershopId(barbershopId);
        return services.stream()
                .map(ServiceResponse::new)
                .collect(Collectors.toList());
    }

    // get all services paginated
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getAllServicesPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Service> servicePage = serviceRepository.findByBarbershopId(barbershopId, pageable);

        return PaginationUtils.createPageResponse(servicePage, ServiceResponse::new);
    }

    // get active services non-paginated
    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServices(UUID barbershopId) {
        List<Service> services = serviceRepository.findByBarbershopIdAndIsActive(barbershopId, true);
        return services.stream()
                .map(ServiceResponse::new)
                .collect(Collectors.toList());
    }

    // get active services paginated
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> getActiveServicesPaginated(UUID barbershopId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Service> servicePage = serviceRepository.findByBarbershopIdAndIsActive(barbershopId, true, pageable);

        return PaginationUtils.createPageResponse(servicePage, ServiceResponse::new);
    }

    // search services by name or description
    @Transactional(readOnly = true)
    public PageResponse<ServiceResponse> searchServices(UUID barbershopId, String searchTerm, int page, int size,
            boolean activeOnly) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // return all if empty search
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return activeOnly ? getActiveServicesPaginated(barbershopId, page, size)
                    : getAllServicesPaginated(barbershopId, page, size);
        }

        Page<Service> servicePage = activeOnly
                ? serviceRepository.searchActiveServices(barbershopId, searchTerm.trim(), pageable)
                : serviceRepository.searchServices(barbershopId, searchTerm.trim(), pageable);

        return PaginationUtils.createPageResponse(servicePage, ServiceResponse::new);
    }

    // get service by id
    @Transactional(readOnly = true)
    public ServiceResponse getService(UUID serviceId, UUID barbershopId) {
        // find service with auth check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        return new ServiceResponse(service);
    }

    // update service
    @Transactional
    public ServiceResponse updateService(UUID serviceId, UUID barbershopId, UpdateServiceRequest request) {
        // find service with auth check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // update fields with validation
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            // check duplicate name
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

        // save updated service
        Service updatedService = serviceRepository.save(service);

        return new ServiceResponse(updatedService);
    }

    // delete service (soft)
    @Transactional
    public void deleteService(UUID serviceId, UUID barbershopId) {
        // find service with auth check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // soft delete
        service.setIsActive(false);
        serviceRepository.save(service);
    }

    // hard delete service
    @Transactional
    public void hardDeleteService(UUID serviceId, UUID barbershopId) {
        // find service with auth check
        Service service = serviceRepository.findByIdAndBarbershopId(serviceId, barbershopId)
                .orElseThrow(() -> new RuntimeException("Service not found or does not belong to this barbershop"));

        // todo: check for existing appointments

        // hard delete
        serviceRepository.delete(service);
    }
}
