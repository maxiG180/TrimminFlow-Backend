package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Barber;
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
public interface BarberRepository extends JpaRepository<Barber, UUID> {

       Page<Barber> findByBarbershopId(UUID barbershopId, Pageable pageable);

       List<Barber> findByBarbershopId(UUID barbershopId);

       Page<Barber> findByBarbershopIdAndIsActiveTrue(UUID barbershopId, Pageable pageable);

       List<Barber> findByBarbershopIdAndIsActiveTrue(UUID barbershopId);

       Optional<Barber> findByIdAndBarbershopId(UUID id, UUID barbershopId);

       boolean existsByEmailAndBarbershopId(String email, UUID barbershopId);

       @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Barber b " +
                     "WHERE b.email = :email AND b.barbershop.id = :barbershopId AND b.id != :barberId")
       boolean existsByEmailAndBarbershopIdExcludingId(@Param("email") String email,
                     @Param("barbershopId") UUID barbershopId,
                     @Param("barberId") UUID barberId);

       @Query("SELECT b FROM Barber b WHERE b.barbershop.id = :barbershopId " +
                     "AND (LOWER(b.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(b.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       Page<Barber> searchBarbers(@Param("barbershopId") UUID barbershopId,
                     @Param("searchTerm") String searchTerm,
                     Pageable pageable);

       @Query("SELECT b FROM Barber b WHERE b.barbershop.id = :barbershopId " +
                     "AND b.isActive = true " +
                     "AND (LOWER(b.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(b.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                     "OR LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
       Page<Barber> searchActiveBarbers(@Param("barbershopId") UUID barbershopId,
                     @Param("searchTerm") String searchTerm,
                     Pageable pageable);
}
