package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomRequest;
import com.hotel.hotelreservation.dto.AdminCatalogDtos.RoomResponse;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.model.Room;
import com.hotel.hotelreservation.model.RoomStatus;
import com.hotel.hotelreservation.model.RoomType;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Transactional
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping("/api/rooms")
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return roomRepository.findAll().stream()
                .filter(room -> status == null || room.getStatus() == status)
                .filter(room -> roomTypeId == null || room.getRoomType().getId().equals(roomTypeId))
                .filter(room -> isFreeForDates(room, checkIn, checkOut))
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/rooms/{id}")
    @Transactional(readOnly = true)
    public RoomResponse getRoom(@PathVariable Long id) {
        return roomRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    @PostMapping("/api/admin/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse createRoom(@Valid @RequestBody RoomRequest request) {
        ensureRoomNumberAvailable(request.roomNumber(), null);
        Room saved = roomRepository.save(Room.builder()
                .roomNumber(request.roomNumber().trim())
                .floor(request.floor())
                .roomType(getRoomType(request.roomTypeId()))
                .status(request.status() == null ? RoomStatus.AVAILABLE : request.status())
                .build());
        return toResponse(saved);
    }

    @PutMapping("/api/admin/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        ensureRoomNumberAvailable(request.roomNumber(), id);
        room.setRoomNumber(request.roomNumber().trim());
        room.setFloor(request.floor());
        room.setRoomType(getRoomType(request.roomTypeId()));
        room.setStatus(request.status() == null ? RoomStatus.AVAILABLE : request.status());
        return toResponse(roomRepository.save(room));
    }

    @PostMapping("/api/admin/rooms/{id}/mark-maintenance")
    public RoomResponse markMaintenance(@PathVariable Long id) {
        Room room = findRoom(id);
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Occupied rooms cannot be marked for maintenance");
        }
        room.setStatus(RoomStatus.MAINTENANCE);
        return toResponse(roomRepository.save(room));
    }

    @PostMapping("/api/admin/rooms/{id}/mark-available")
    public RoomResponse markAvailable(@PathVariable Long id) {
        Room room = findRoom(id);
        room.setStatus(RoomStatus.AVAILABLE);
        return toResponse(roomRepository.save(room));
    }

    private boolean isFreeForDates(Room room, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return true;
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkIn must be before checkOut");
        }
        boolean assignedOverlap = reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.getRoom() != null &&
                        reservation.getRoom().getId().equals(room.getId()) &&
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN).contains(reservation.getStatus()) &&
                        !(reservation.getCheckOutDate().isBefore(checkIn.plusDays(1)) ||
                                reservation.getCheckInDate().isAfter(checkOut.minusDays(1))));
        return !assignedOverlap;
    }

    private Room findRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    private RoomType getRoomType(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room type not found"));
    }

    private void ensureRoomNumberAvailable(String roomNumber, Long currentId) {
        roomRepository.findByRoomNumber(roomNumber.trim())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Room number already exists");
                });
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getRoomType() != null ? room.getRoomType().getId() : null,
                room.getRoomType() != null ? room.getRoomType().getName() : null,
                room.getStatus()
        );
    }
}
