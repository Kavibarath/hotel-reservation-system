package com.hotel.hotelreservation.service.impl;

import com.hotel.hotelreservation.model.RatePlan;
import com.hotel.hotelreservation.model.RoomType;
import com.hotel.hotelreservation.repository.SeasonRepository;
import com.hotel.hotelreservation.service.PricingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PricingEngineImpl implements PricingEngine {

    private final SeasonRepository seasonRepository;

    @Override
    public BigDecimal calculateTotalBasePrice(RoomType roomType,
                                              LocalDate checkIn,
                                              LocalDate checkOut) {

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            BigDecimal multiplier = seasonRepository.findActiveSeasons(date)
                    .stream()
                    .map(season -> season.getMultiplier() == null ? BigDecimal.ONE : season.getMultiplier())
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);

            total = total.add(roomType.getBasePricePerNight().multiply(multiplier));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTotalWithRatePlan(RoomType roomType,
                                                 LocalDate checkIn,
                                                 LocalDate checkOut,
                                                 RatePlan ratePlan) {

        BigDecimal baseTotal = calculateTotalBasePrice(roomType, checkIn, checkOut);

        if (ratePlan != null && ratePlan.getPriceModifier() != null) {
            return baseTotal.add(ratePlan.getPriceModifier()).setScale(2, RoundingMode.HALF_UP);
        }
        return baseTotal;
    }
}
