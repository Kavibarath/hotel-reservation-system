package com.hotel.hotelreservation.dto;

import com.hotel.hotelreservation.model.RoomStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class AdminCatalogDtos {

    private AdminCatalogDtos() {
    }

    public record RoomTypeRequest(
            @NotBlank String name,
            String description,
            @NotNull @Min(1) Integer maxOccupancy,
            String bedType,
            Double sizeSqm,
            @NotNull @DecimalMin("0.00") BigDecimal basePricePerNight,
            String amenities,
            String images
    ) {
    }

    public record RoomTypeResponse(
            Long id,
            String name,
            String description,
            Integer maxOccupancy,
            String bedType,
            Double sizeSqm,
            BigDecimal basePricePerNight,
            String amenities,
            String images
    ) {
    }

    public record RoomRequest(
            @NotBlank String roomNumber,
            Integer floor,
            @NotNull Long roomTypeId,
            RoomStatus status
    ) {
    }

    public record RoomResponse(
            Long id,
            String roomNumber,
            Integer floor,
            Long roomTypeId,
            String roomTypeName,
            RoomStatus status
    ) {
    }

    public record RatePlanRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.00") BigDecimal priceModifier,
            String cancellationPolicy
    ) {
    }

    public record RatePlanResponse(
            Long id,
            String name,
            String description,
            BigDecimal priceModifier,
            String cancellationPolicy
    ) {
    }
}
