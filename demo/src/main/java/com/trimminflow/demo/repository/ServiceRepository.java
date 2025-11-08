package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ServiceRepository
 *
 * Provides database operations for Service entity
 * Spring Data JPA automatically implements basic CRUD methods
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    /**
     * Find all services for a specific barbershop
     *
     * @param barbershopId The barbershop's UUID
     * @return List of services belonging to that barbershop
     */
    List<Service> findByBarbershopId(UUID barbershopId);

    /**
     * Find all active services for a specific barbershop
     *
     * @param barbershopId The barbershop's UUID
     * @param isActive Filter by active status
     * @return List of active services
     */
    List<Service> findByBarbershopIdAndIsActive(UUID barbershopId, Boolean isActive);
}
