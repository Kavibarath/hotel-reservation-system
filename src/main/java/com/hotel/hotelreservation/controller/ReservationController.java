package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.CreateReservationRequest;
import com.hotel.hotelreservation.dto.ReservationResponse;
import com.hotel.hotelreservation.dto.ReservationSummary;
import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.model.Role;
import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import com.hotel.hotelreservation.repository.UserRepository;
import com.hotel.hotelreservation.service.PricingEngine;
import com.hotel.hotelreservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final PricingEngine pricingEngine;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest req) {
        return reservationService.createReservation(req);
    }

    @GetMapping("/my")
    public List<ReservationSummary> listMyReservations(@RequestParam Long userId) {
        var reservations = reservationService.listReservationsForUser(userId);
        return reservations.stream().map(this::toSummary).toList();
    }

    @GetMapping
    public List<ReservationSummary> listReservations(@RequestParam Long userId, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ensureCanAccessUser(user, authentication);
        return reservationRepository.findByUserId(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservationSummary getReservation(@PathVariable Long id, Authentication authentication) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        ensureCanAccessUser(reservation.getUser(), authentication);
        return toSummary(reservation);
    }

    @PutMapping("/{id}")
    public ReservationResponse updatePendingReservation(
            @PathVariable Long id,
            @Valid @RequestBody CreateReservationRequest req,
            Authentication authentication
    ) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        ensureCanAccessUser(reservation.getUser(), authentication);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING reservations can be modified");
        }
        if (!req.getCheckInDate().isBefore(req.getCheckOutDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate must be before checkOutDate");
        }
        if (req.getCheckInDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate cannot be in the past");
        }

        var roomType = roomTypeRepository.findById(req.getRoomTypeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found"));
        if (roomType.getMaxOccupancy() != null && req.getGuests() > roomType.getMaxOccupancy()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guests exceed max occupancy");
        }
        var ratePlan = req.getRatePlanId() == null ? null : ratePlanRepository.findById(req.getRatePlanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found"));

        reservation.setRoomType(roomType);
        reservation.setCheckInDate(req.getCheckInDate());
        reservation.setCheckOutDate(req.getCheckOutDate());
        reservation.setNumberOfGuests(req.getGuests());
        reservation.setRatePlan(ratePlan);
        reservation.setTotalPrice(pricingEngine.calculateTotalWithRatePlan(roomType, req.getCheckInDate(), req.getCheckOutDate(), ratePlan));
        return toResponse(reservationRepository.save(reservation));
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(@PathVariable Long reservationId) {
        Reservation saved = reservationService.cancelReservation(reservationId);
        return toResponse(saved);
    }

    private ReservationSummary toSummary(Reservation r) {
        return new ReservationSummary(
                r.getId(),
                r.getReservationCode(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getRoomType() != null ? r.getRoomType().getId() : null,
                r.getRoomType() != null ? r.getRoomType().getName() : null,
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getStatus().name(),
                r.getTotalPrice()
        );
    }

    private ReservationResponse toResponse(Reservation saved) {
        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser() != null ? saved.getUser().getId() : null,
                saved.getRoomType() != null ? saved.getRoomType().getId() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTotalPrice()
        );
    }

    private void ensureCanAccessUser(User user, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        boolean elevated = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") ||
                        authority.getAuthority().equals("ROLE_STAFF"));
        if (elevated) {
            return;
        }
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> claims && claims.get("userId") instanceof Number userId &&
                user.getId().equals(userId.longValue())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this reservation");
    }
}
