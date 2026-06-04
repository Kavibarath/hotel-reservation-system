package com.hotel.hotelreservation.dto;

import lombok.Value;

import java.math.BigDecimal;

/**
 * Response after creating or fetching a reservation summary.
 */
@Value
public class ReservationResponse {
    Long reservationId;
    String reservationCode;
    Long userId;
    Long roomTypeId;
    String status;
    BigDecimal totalPrice;
}
