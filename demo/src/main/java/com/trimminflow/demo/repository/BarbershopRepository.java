package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BarbershopRepository extends JpaRepository<Barbershop, UUID> {
}
