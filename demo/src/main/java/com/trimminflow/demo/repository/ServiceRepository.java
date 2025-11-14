package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
     * Find all services for a specific barbershop with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param pageable Pagination parameters
     * @return Page of services
     */
    Page<Service> findByBarbershopId(UUID barbershopId, Pageable pageable);

    /**
     * Find all active services for a specific barbershop
     *
     * @param barbershopId The barbershop's UUID
     * @param isActive Filter by active status
     * @return List of active services
     */
    List<Service> findByBarbershopIdAndIsActive(UUID barbershopId, Boolean isActive);

    /**
     * Find all active services for a specific barbershop with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param isActive Filter by active status
     * @param pageable Pagination parameters
     * @return Page of active services
     */
    Page<Service> findByBarbershopIdAndIsActive(UUID barbershopId, Boolean isActive, Pageable pageable);

    /**
     * Search services by name or description with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param searchTerm The search term
     * @param pageable Pagination parameters
     * @return Page of matching services
     */
    @Query("SELECT s FROM Service s WHERE s.barbershop.id = :barbershopId " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Service> searchServices(@Param("barbershopId") UUID barbershopId,
                                  @Param("searchTerm") String searchTerm,
                                  Pageable pageable);

    /**
     * Search active services by name or description with pagination
     *
     * @param barbershopId The barbershop's UUID
     * @param searchTerm The search term
     * @param pageable Pagination parameters
     * @return Page of matching active services
     */
    @Query("SELECT s FROM Service s WHERE s.barbershop.id = :barbershopId " +
           "AND s.isActive = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Service> searchActiveServices(@Param("barbershopId") UUID barbershopId,
                                        @Param("searchTerm") String searchTerm,
                                        Pageable pageable);

    /**
     * Find service by ID and barbershop ID (for authorization)
     *
     * @param id The service's UUID
     * @param barbershopId The barbershop's UUID
     * @return Optional of Service
     */
    Optional<Service> findByIdAndBarbershopId(UUID id, UUID barbershopId);

    /**
     * Check if service name already exists for barbershop
     *
     * @param name The service name
     * @param barbershopId The barbershop's UUID
     * @return true if exists
     */
    boolean existsByNameAndBarbershopId(String name, UUID barbershopId);

    /**
     * Check if service name exists for another service in the same barbershop
     * (used when updating to prevent duplicate names)
     *
     * @param name The service name
     * @param barbershopId The barbershop's UUID
     * @param serviceId The current service's UUID to exclude
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Service s " +
           "WHERE s.name = :name AND s.barbershop.id = :barbershopId AND s.id != :serviceId")
    boolean existsByNameAndBarbershopIdExcludingId(@Param("name") String name,
                                                     @Param("barbershopId") UUID barbershopId,
                                                     @Param("serviceId") UUID serviceId);
}
