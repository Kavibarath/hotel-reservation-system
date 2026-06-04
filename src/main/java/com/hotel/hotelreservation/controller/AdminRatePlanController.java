package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AdminCatalogDtos.RatePlanRequest;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RatePlanResponse;
import com.hotel.hotelreservation.model.RatePlan;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/rate-plans")
@RequiredArgsConstructor
@Transactional
public class AdminRatePlanController {

    private final RatePlanRepository ratePlanRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatePlanResponse create(@Valid @RequestBody RatePlanRequest request) {
        ensureNameAvailable(request.name(), null);
        return toResponse(ratePlanRepository.save(RatePlan.builder()
                .name(request.name().trim())
                .description(request.description())
                .priceModifier(request.priceModifier())
                .cancellationPolicy(request.cancellationPolicy())
                .build()));
    }

    @PutMapping("/{id}")
    public RatePlanResponse update(@PathVariable Long id, @Valid @RequestBody RatePlanRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found"));
        ensureNameAvailable(request.name(), id);
        ratePlan.setName(request.name().trim());
        ratePlan.setDescription(request.description());
        ratePlan.setPriceModifier(request.priceModifier());
        ratePlan.setCancellationPolicy(request.cancellationPolicy());
        return toResponse(ratePlanRepository.save(ratePlan));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!ratePlanRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found");
        }
        if (reservationRepository.existsByRatePlanId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rate plan is in use and cannot be deleted");
        }
        ratePlanRepository.deleteById(id);
    }

    private void ensureNameAvailable(String name, Long currentId) {
        ratePlanRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Rate plan name already exists");
                });
    }

    private RatePlanResponse toResponse(RatePlan ratePlan) {
        return new RatePlanResponse(ratePlan.getId(), ratePlan.getName(), ratePlan.getDescription(),
                ratePlan.getPriceModifier(), ratePlan.getCancellationPolicy());
    }
}
