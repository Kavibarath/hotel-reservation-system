package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.RatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Basic repository for rate plans.
 */
@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {

    Optional<RatePlan> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
