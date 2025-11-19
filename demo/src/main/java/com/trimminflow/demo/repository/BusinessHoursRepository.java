package com.trimminflow.demo.repository;

import com.trimminflow.demo.entity.BusinessHours;
import com.trimminflow.demo.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    List<BusinessHours> findByBarbershopId(UUID barbershopId);
    Optional<BusinessHours> findByBarbershopIdAndDayOfWeek(UUID barbershopId, DayOfWeek dayOfWeek);
}
