package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AdminReservationViewResponse;
import com.hotel.hotelreservation.dto.ReservationResponse;
import com.hotel.hotelreservation.dto.RoomReassignmentResponse;
import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<AdminReservationViewResponse> getAllReservations() {
        return reservationService.getAllReservationsForAdmin();
    }

    @GetMapping("/{id}")
    public AdminReservationViewResponse getReservationById(@PathVariable Long id) {
        return reservationService.getReservationDetailsForAdmin(id);
    }

    @GetMapping("/search")
    public AdminReservationViewResponse searchByReservationCode(@RequestParam String reservationCode) {
        return reservationService.searchReservationByCode(reservationCode);
    }

    @GetMapping("/filter")
    public List<AdminReservationViewResponse> filterReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate
    ) {
        return reservationService.filterReservationsForAdmin(status, userId, roomTypeId, checkInDate);
    }

    @PostMapping("/{id}/check-in")
    public ReservationResponse checkIn(@PathVariable Long id) {
        Reservation saved = reservationService.checkIn(id);
        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getRoomType() != null ? saved.getRoomType().getId() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTotalPrice()
        );
    }

    @PostMapping("/{id}/check-out")
    public ReservationResponse checkOut(@PathVariable Long id) {
        Reservation saved = reservationService.checkOut(id);
        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getRoomType() != null ? saved.getRoomType().getId() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTotalPrice()
        );
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservationByAdmin(@PathVariable Long reservationId) {
        Reservation saved = reservationService.cancelReservation(reservationId);
        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getRoomType() != null ? saved.getRoomType().getId() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTotalPrice()
        );
    }

    @PostMapping("/{reservationId}/no-show")
    public ReservationResponse markNoShow(@PathVariable Long reservationId) {
        Reservation saved = reservationService.markNoShow(reservationId);
        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getRoomType() != null ? saved.getRoomType().getId() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTotalPrice()
        );
    }

    @PostMapping("/{reservationId}/change-room")
    public RoomReassignmentResponse changeRoom(
            @PathVariable Long reservationId,
            @RequestParam Long newRoomId
    ) {
        return reservationService.changeRoom(reservationId, newRoomId);
    }
}