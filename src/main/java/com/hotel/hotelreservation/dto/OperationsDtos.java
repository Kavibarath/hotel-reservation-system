package com.hotel.hotelreservation.dto;

import com.hotel.hotelreservation.model.CleaningTaskStatus;
import com.hotel.hotelreservation.model.MaintenanceRequestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class OperationsDtos {

    private OperationsDtos() {
    }

    public record CleaningTaskResponse(
            Long id,
            Long roomId,
            String roomNumber,
            CleaningTaskStatus status,
            Long assignedStaffId,
            String assignedStaffName,
            Instant createdAt,
            Instant completedAt
    ) {
    }

    public record MaintenanceRequestRequest(
            @NotNull Long roomId,
            Long createdByUserId,
            @NotBlank String description
    ) {
    }

    public record MaintenanceRequestResponse(
            Long id,
            Long roomId,
            String roomNumber,
            String description,
            MaintenanceRequestStatus status,
            Long createdByUserId,
            Long resolvedByUserId,
            Instant createdAt,
            Instant resolvedAt
    ) {
    }

    public record DashboardSummaryResponse(
            long totalReservations,
            long activeBookings,
            long availableRooms,
            long occupiedRooms,
            long maintenanceRooms,
            java.math.BigDecimal totalRevenue
    ) {
    }

    public record DailyRevenueResponse(
            java.time.LocalDate date,
            java.math.BigDecimal revenue
    ) {
    }
}
