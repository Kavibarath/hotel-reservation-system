package com.hotel.hotelreservation.service;

import com.hotel.hotelreservation.dto.AdminReservationViewResponse;
import com.hotel.hotelreservation.dto.CreateReservationRequest;
import com.hotel.hotelreservation.dto.ReservationResponse;
import com.hotel.hotelreservation.dto.RoomReassignmentResponse;
import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest req);

    List<Reservation> listReservationsForUser(Long userId);

    Reservation checkIn(Long reservationId);

    Reservation checkOut(Long reservationId);

    Reservation cancelReservation(Long reservationId);

    Reservation markNoShow(Long reservationId);

    RoomReassignmentResponse changeRoom(Long reservationId, Long newRoomId);

    List<AdminReservationViewResponse> getAllReservationsForAdmin();

    AdminReservationViewResponse getReservationDetailsForAdmin(Long reservationId);

    AdminReservationViewResponse searchReservationByCode(String reservationCode);

    List<AdminReservationViewResponse> filterReservationsForAdmin(
            ReservationStatus status,
            Long userId,
            Long roomTypeId,
            LocalDate checkInDate
    );
}