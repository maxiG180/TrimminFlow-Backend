package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByBarbershopIdAndPhone(UUID barbershopId, String phone);

    Optional<Customer> findByBarbershopIdAndEmail(UUID barbershopId, String email);

    Page<Customer> findByBarbershopId(UUID barbershopId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.barbershop.id = :barbershopId AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> searchCustomers(
            @Param("barbershopId") UUID barbershopId,
            @Param("search") String search,
            Pageable pageable);
}
