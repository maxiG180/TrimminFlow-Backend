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

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

       List<Service> findByBarbershopId(UUID barbershopId);

       Page<Service> findByBarbershopId(UUID barbershopId, Pageable pageable);

       List<Service> findByBarbershopIdAndIsActive(UUID barbershopId, Boolean isActive);

       Page<Service> findByBarbershopIdAndIsActive(UUID barbershopId, Boolean isActive, Pageable pageable);

       @Query("SELECT s FROM Service s WHERE s.barbershop.id = :barbershopId " +
                     "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       Page<Service> searchServices(@Param("barbershopId") UUID barbershopId,
                     @Param("searchTerm") String searchTerm,
                     Pageable pageable);

       @Query("SELECT s FROM Service s WHERE s.barbershop.id = :barbershopId " +
                     "AND s.isActive = true " +
                     "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       Page<Service> searchActiveServices(@Param("barbershopId") UUID barbershopId,
                     @Param("searchTerm") String searchTerm,
                     Pageable pageable);

       Optional<Service> findByIdAndBarbershopId(UUID id, UUID barbershopId);

       boolean existsByNameAndBarbershopId(String name, UUID barbershopId);

       @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Service s " +
                     "WHERE s.name = :name AND s.barbershop.id = :barbershopId AND s.id != :serviceId")
       boolean existsByNameAndBarbershopIdExcludingId(@Param("name") String name,
                     @Param("barbershopId") UUID barbershopId,
                     @Param("serviceId") UUID serviceId);
}
