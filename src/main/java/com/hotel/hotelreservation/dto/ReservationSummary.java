package com.hotel.hotelreservation.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class ReservationSummary {
    Long reservationId;
    String reservationCode;
    Long userId;
    Long roomTypeId;
    String roomTypeName;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    String status;
    BigDecimal totalPrice;
}
