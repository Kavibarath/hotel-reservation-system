package com.hotel.hotelreservation.service;

import com.hotel.hotelreservation.dto.AvailabilityResult;
import com.hotel.hotelreservation.dto.SearchRequest;
import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.model.RoomType;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Availability service that returns availability and pricing per RoomType.
 */
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PricingEngine pricingEngine;
    private final RatePlanRepository ratePlanRepository;

    /**
     * Search availability across room types (or a single room type if provided).
     */
    public List<AvailabilityResult> searchAvailability(SearchRequest req) {
        validateSearchRequest(req);

        List<RoomType> types = (req.getRoomTypeId() == null)
                ? roomTypeRepository.findAll()
                : roomTypeRepository.findById(req.getRoomTypeId()).map(List::of).orElse(List.of());

        List<AvailabilityResult> results = new ArrayList<>();
        for (RoomType rt : types) {
            // occupancy check
            boolean fitsOccupancy = req.getGuests() <= (rt.getMaxOccupancy() == null ? Integer.MAX_VALUE : rt.getMaxOccupancy());
            if (!fitsOccupancy) {
                results.add(new AvailabilityResult(rt.getId(), rt.getName(), rt.getBasePricePerNight(), BigDecimal.ZERO, false));
                continue;
            }

            // inventory check: how many rooms of this type exist
            long totalRooms = roomRepository.countByRoomType(rt);

            // count overlapping reservations for active statuses (CONFIRMED, CHECKED_IN)
            long reservedCount = reservationRepository.countOverlappingReservations(rt, req.getCheckInDate(), req.getCheckOutDate());

            boolean available = reservedCount < totalRooms;

            // price calculation
            BigDecimal totalPrice = pricingEngine.calculateTotalBasePrice(rt, req.getCheckInDate(), req.getCheckOutDate());
            // average nightly price (defensive: if 0 nights, price = 0)
            long nights = ChronoUnit.DAYS.between(req.getCheckInDate(), req.getCheckOutDate());
            BigDecimal pricePerNight = (nights <= 0) ? BigDecimal.ZERO : totalPrice.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);

            // if a default/first rate plan should be applied, you could fetch it here;
            // for now we return base nightly price and totalPrice (without rate plan)
            results.add(new AvailabilityResult(rt.getId(), rt.getName(), pricePerNight, totalPrice, available));
        }
        return results;
    }

    /**
     * Helper that returns availability + computed total using a specific rate plan id (optional).
     * Useful for when a user selects a rate plan before booking.
     */
    public AvailabilityResult availabilityWithRatePlan(Long roomTypeId, SearchRequest req, Long ratePlanId) {
        validateSearchRequest(req);

        RoomType rt = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found"));
        long totalRooms = roomRepository.countByRoomType(rt);
        long reservedCount = reservationRepository.countOverlappingReservations(rt, req.getCheckInDate(), req.getCheckOutDate());
        boolean fitsOccupancy = req.getGuests() <= (rt.getMaxOccupancy() == null ? Integer.MAX_VALUE : rt.getMaxOccupancy());
        boolean available = fitsOccupancy && reservedCount < totalRooms;

        var rp = (ratePlanId == null) ? null : ratePlanRepository.findById(ratePlanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found"));
        BigDecimal totalPrice = pricingEngine.calculateTotalWithRatePlan(rt, req.getCheckInDate(), req.getCheckOutDate(), rp);

        long nights = java.time.temporal.ChronoUnit.DAYS.between(req.getCheckInDate(), req.getCheckOutDate());
        BigDecimal pricePerNight = (nights <= 0) ? BigDecimal.ZERO : totalPrice.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);

        return new AvailabilityResult(rt.getId(), rt.getName(), pricePerNight, totalPrice, available);
    }

    private void validateSearchRequest(SearchRequest req) {
        if (req.getCheckInDate() == null || req.getCheckOutDate() == null ||
                !req.getCheckInDate().isBefore(req.getCheckOutDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate must be before checkOutDate");
        }

        if (req.getGuests() == null || req.getGuests() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "guests must be at least 1");
        }
    }
}

