package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AvailabilityResult;
import com.hotel.hotelreservation.dto.SearchRequest;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import com.hotel.hotelreservation.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints: search availability, etc.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class PublicController {

    private final AvailabilityService availabilityService;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;

    @GetMapping("/room-types")
    public List<RoomTypeCatalogResponse> listRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(roomType -> new RoomTypeCatalogResponse(
                        roomType.getId(),
                        roomType.getName(),
                        roomType.getDescription(),
                        roomType.getMaxOccupancy(),
                        roomType.getBedType(),
                        roomType.getSizeSqm(),
                        roomType.getBasePricePerNight(),
                        roomType.getAmenities(),
                        roomType.getImages()
                ))
                .toList();
    }

    @GetMapping("/rate-plans")
    public List<RatePlanCatalogResponse> listRatePlans() {
        return ratePlanRepository.findAll().stream()
                .map(ratePlan -> new RatePlanCatalogResponse(
                        ratePlan.getId(),
                        ratePlan.getName(),
                        ratePlan.getDescription(),
                        ratePlan.getPriceModifier(),
                        ratePlan.getCancellationPolicy()
                ))
                .toList();
    }

    @PostMapping("/search-availability")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailabilityResult> searchAvailability(@Valid @RequestBody SearchRequest req) {
        return availabilityService.searchAvailability(req);
    }

    @PostMapping("/search-availability/{roomTypeId}")
    public AvailabilityResult searchWithRatePlan(@PathVariable Long roomTypeId,
                                                 @RequestParam(required = false) Long ratePlanId,
                                                 @Valid @RequestBody SearchRequest req) {
        return availabilityService.availabilityWithRatePlan(roomTypeId, req, ratePlanId);
    }

    public record RoomTypeCatalogResponse(
            Long id,
            String name,
            String description,
            Integer maxOccupancy,
            String bedType,
            Double sizeSqm,
            java.math.BigDecimal basePricePerNight,
            String amenities,
            String images
    ) {
    }

    public record RatePlanCatalogResponse(
            Long id,
            String name,
            String description,
            java.math.BigDecimal priceModifier,
            String cancellationPolicy
    ) {
    }
}

