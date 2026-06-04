package com.hotel.hotelreservation.dto;

import lombok.Value;

import java.math.BigDecimal;

/**
 * Availability result per room type.
 */
@Value
public class AvailabilityResult {
    Long roomTypeId;
    String roomTypeName;
    BigDecimal pricePerNight; // average price per night across the requested nights
    BigDecimal totalPrice;    // total for the full stay (before taxes/fees)
    boolean available;



}

