package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AdminCatalogDtos.RatePlanRequest;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RatePlanResponse;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomRequest;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomResponse;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomTypeRequest;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomTypeResponse;
import com.hotel.hotelreservation.model.RatePlan;
import com.hotel.hotelreservation.model.Room;
import com.hotel.hotelreservation.model.RoomStatus;
import com.hotel.hotelreservation.model.RoomType;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/catalog")
@RequiredArgsConstructor
@Transactional
public class AdminCatalogController {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final RatePlanRepository ratePlanRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping("/room-types")
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> listRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::toRoomTypeResponse)
                .toList();
    }

    @PostMapping("/room-types")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomTypeResponse createRoomType(@Valid @RequestBody RoomTypeRequest request) {
        ensureRoomTypeNameAvailable(request.name(), null);

        RoomType saved = roomTypeRepository.save(RoomType.builder()
                .name(request.name().trim())
                .description(request.description())
                .maxOccupancy(request.maxOccupancy())
                .bedType(request.bedType())
                .sizeSqm(request.sizeSqm())
                .basePricePerNight(request.basePricePerNight())
                .amenities(request.amenities())
                .images(request.images())
                .build());

        return toRoomTypeResponse(saved);
    }

    @PutMapping("/room-types/{id}")
    public RoomTypeResponse updateRoomType(@PathVariable Long id, @Valid @RequestBody RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found"));
        ensureRoomTypeNameAvailable(request.name(), id);

        roomType.setName(request.name().trim());
        roomType.setDescription(request.description());
        roomType.setMaxOccupancy(request.maxOccupancy());
        roomType.setBedType(request.bedType());
        roomType.setSizeSqm(request.sizeSqm());
        roomType.setBasePricePerNight(request.basePricePerNight());
        roomType.setAmenities(request.amenities());
        roomType.setImages(request.images());
        return toRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    @DeleteMapping("/room-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoomType(@PathVariable Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found");
        }
        if (roomRepository.existsByRoomTypeId(id) || reservationRepository.existsByRoomTypeId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room type is in use and cannot be deleted");
        }
        roomTypeRepository.deleteById(id);
    }

    @GetMapping("/rooms")
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms() {
        return roomRepository.findAll().stream()
                .map(this::toRoomResponse)
                .toList();
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody RoomRequest request) {
        ensureRoomNumberAvailable(request.roomNumber(), null);
        RoomType roomType = getRoomType(request.roomTypeId());

        Room saved = roomRepository.save(Room.builder()
                .roomNumber(request.roomNumber().trim())
                .floor(request.floor())
                .roomType(roomType)
                .status(request.status() == null ? RoomStatus.AVAILABLE : request.status())
                .build());

        return toRoomResponse(saved);
    }

    @PutMapping("/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        ensureRoomNumberAvailable(request.roomNumber(), id);

        room.setRoomNumber(request.roomNumber().trim());
        room.setFloor(request.floor());
        room.setRoomType(getRoomType(request.roomTypeId()));
        room.setStatus(request.status() == null ? RoomStatus.AVAILABLE : request.status());
        return toRoomResponse(roomRepository.save(room));
    }

    @DeleteMapping("/rooms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        if (reservationRepository.existsByRoomId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is assigned to reservations and cannot be deleted");
        }
        roomRepository.deleteById(id);
    }

    @GetMapping("/rate-plans")
    @Transactional(readOnly = true)
    public List<RatePlanResponse> listRatePlans() {
        return ratePlanRepository.findAll().stream()
                .map(this::toRatePlanResponse)
                .toList();
    }

    @PostMapping("/rate-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public RatePlanResponse createRatePlan(@Valid @RequestBody RatePlanRequest request) {
        ensureRatePlanNameAvailable(request.name(), null);

        RatePlan saved = ratePlanRepository.save(RatePlan.builder()
                .name(request.name().trim())
                .description(request.description())
                .priceModifier(request.priceModifier())
                .cancellationPolicy(request.cancellationPolicy())
                .build());

        return toRatePlanResponse(saved);
    }

    @PutMapping("/rate-plans/{id}")
    public RatePlanResponse updateRatePlan(@PathVariable Long id, @Valid @RequestBody RatePlanRequest request) {
        RatePlan ratePlan = ratePlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found"));
        ensureRatePlanNameAvailable(request.name(), id);

        ratePlan.setName(request.name().trim());
        ratePlan.setDescription(request.description());
        ratePlan.setPriceModifier(request.priceModifier());
        ratePlan.setCancellationPolicy(request.cancellationPolicy());
        return toRatePlanResponse(ratePlanRepository.save(ratePlan));
    }

    @DeleteMapping("/rate-plans/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRatePlan(@PathVariable Long id) {
        if (!ratePlanRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rate plan not found");
        }
        if (reservationRepository.existsByRatePlanId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rate plan is in use and cannot be deleted");
        }
        ratePlanRepository.deleteById(id);
    }

    private RoomType getRoomType(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found"));
    }

    private void ensureRoomTypeNameAvailable(String name, Long currentId) {
        roomTypeRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Room type name already exists");
                });
    }

    private void ensureRoomNumberAvailable(String roomNumber, Long currentId) {
        roomRepository.findByRoomNumber(roomNumber.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Room number already exists");
                });
    }

    private void ensureRatePlanNameAvailable(String name, Long currentId) {
        ratePlanRepository.findByNameIgnoreCase(name.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Rate plan name already exists");
                });
    }

    private RoomTypeResponse toRoomTypeResponse(RoomType roomType) {
        return new RoomTypeResponse(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getMaxOccupancy(),
                roomType.getBedType(),
                roomType.getSizeSqm(),
                roomType.getBasePricePerNight(),
                roomType.getAmenities(),
                roomType.getImages()
        );
    }

    private RoomResponse toRoomResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getRoomType() != null ? room.getRoomType().getId() : null,
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                room.getStatus()
        );
    }

    private RatePlanResponse toRatePlanResponse(RatePlan ratePlan) {
        return new RatePlanResponse(
                ratePlan.getId(),
                ratePlan.getName(),
                ratePlan.getDescription(),
                ratePlan.getPriceModifier(),
                ratePlan.getCancellationPolicy()
        );
    }
}
