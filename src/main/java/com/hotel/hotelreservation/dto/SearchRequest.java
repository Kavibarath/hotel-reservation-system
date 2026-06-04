package com.hotel.hotelreservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.time.LocalDate;

/**
 * Request for availability search.
 */
@Value
public class SearchRequest {
    @NotNull
    LocalDate checkInDate;

    @NotNull
    LocalDate checkOutDate;

    @NotNull
    Integer guests;

    Long roomTypeId; // optional: limit to specific room type
}

