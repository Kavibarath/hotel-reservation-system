package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RoomType. JpaSpecificationExecutor allows dynamic filtering
 * (e.g., by maxOccupancy, price ranges) without writing many custom methods.
 */
@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long>,
        JpaSpecificationExecutor<RoomType> {

    Optional<RoomType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
