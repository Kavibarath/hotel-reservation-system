package com.hotel.hotelreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomReassignmentResponse {
    private Long reservationId;
    private String reservationCode;
    private Long oldRoomId;
    private String oldRoomNumber;
    private Long newRoomId;
    private String newRoomNumber;
    private String status;
    private BigDecimal totalPrice;
    private String message;
}