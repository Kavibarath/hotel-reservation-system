package com.hotel.hotelreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationViewResponse {
    private Long reservationId;
    private String reservationCode;

    private Long userId;
    private String userName;

    private Long roomTypeId;
    private String roomTypeName;

    private Long roomId;
    private String roomNumber;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Integer numberOfGuests;
    private String status;
    private BigDecimal totalPrice;
    private Instant createdAt;
}