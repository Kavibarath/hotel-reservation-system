package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.Room;
import com.hotel.hotelreservation.model.RoomStatus;
import com.hotel.hotelreservation.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for physical rooms.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Count the number of rooms for a given room type.
     * Useful when comparing booked rooms vs total inventory.
     */
    long countByRoomType(RoomType roomType);

    /**
     * Find rooms by their status (AVAILABLE, OCCUPIED, OUT_OF_SERVICE, RESERVED).
     */
    List<Room> findByStatus(RoomStatus status);

    long countByStatus(RoomStatus status);

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomTypeId(Long roomTypeId);

    /**
     * Find rooms by type and status.
     */
    List<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status);
}

