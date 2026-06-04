package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.MaintenanceRequest;
import com.hotel.hotelreservation.model.MaintenanceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    List<MaintenanceRequest> findByStatus(MaintenanceRequestStatus status);
    List<MaintenanceRequest> findByRoomId(Long roomId);
}
