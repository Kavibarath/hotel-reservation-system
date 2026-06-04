package com.hotel.hotelreservation.service;

import com.hotel.hotelreservation.model.RatePlan;
import com.hotel.hotelreservation.model.RoomType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface PricingEngine {

    BigDecimal calculateTotalWithRatePlan(
            RoomType roomType,
            LocalDate checkIn,
            LocalDate checkOut,
            RatePlan ratePlan
    );

    BigDecimal calculateTotalBasePrice(
            RoomType rt, @NotNull LocalDate checkInDate, @NotNull LocalDate checkOutDate);
}
