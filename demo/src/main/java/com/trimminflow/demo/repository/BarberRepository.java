package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Barber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BarberRepository
 *
 * Data access layer for Barber entity
 */
@Repository
public interface BarberRepository extends JpaRepository<Barber, UUID> {

    /**
     * Find all barbers for a specific barbershop (paginated)
     */
    Page<Barber> findByBarbershopId(UUID barbershopId, Pageable pageable);

    /**
     * Find all barbers for a specific barbershop (non-paginated)
     */
    List<Barber> findByBarbershopId(UUID barbershopId);

    /**
     * Find all active barbers for a specific barbershop (paginated)
     */
    Page<Barber> findByBarbershopIdAndIsActiveTrue(UUID barbershopId, Pageable pageable);

    /**
     * Find all active barbers for a specific barbershop (non-paginated)
     */
    List<Barber> findByBarbershopIdAndIsActiveTrue(UUID barbershopId);

    /**
     * Find a specific barber by ID and barbershop ID
     */
    Optional<Barber> findByIdAndBarbershopId(UUID id, UUID barbershopId);

    /**
     * Check if email already exists for a barbershop
     */
    boolean existsByEmailAndBarbershopId(String email, UUID barbershopId);
}
