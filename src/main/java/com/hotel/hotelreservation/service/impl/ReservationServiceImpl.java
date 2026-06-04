package com.hotel.hotelreservation.service.impl;

import com.hotel.hotelreservation.dto.AdminReservationViewResponse;
import com.hotel.hotelreservation.dto.CreateReservationRequest;
import com.hotel.hotelreservation.dto.ReservationResponse;
import com.hotel.hotelreservation.dto.RoomReassignmentResponse;
import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.RatePlanRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.RoomTypeRepository;
import com.hotel.hotelreservation.repository.UserRepository;
import com.hotel.hotelreservation.repository.CleaningTaskRepository;
import com.hotel.hotelreservation.service.PaymentService;
import com.hotel.hotelreservation.service.PricingEngine;
import com.hotel.hotelreservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final RoomRepository roomRepo;
    private final RatePlanRepository ratePlanRepo;
    private final CleaningTaskRepository cleaningTaskRepository;
    private final PricingEngine pricingEngine;
    private final PaymentService paymentService;

    private static final AtomicLong codeCounter =
            new AtomicLong(System.currentTimeMillis() % 1_000_000);

    @Override
    public ReservationResponse createReservation(CreateReservationRequest req) {

        if (req.getCheckInDate() == null || req.getCheckOutDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "checkInDate and checkOutDate are required"
            );
        }

        if (req.getGuests() == null || req.getGuests() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "guests must be at least 1"
            );
        }

        if (!req.getCheckInDate().isBefore(req.getCheckOutDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "checkInDate must be before checkOutDate"
            );
        }

        if (req.getCheckInDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "checkInDate cannot be in the past"
            );
        }

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        RoomType roomType = roomTypeRepo.findById(req.getRoomTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Room type not found"));

        if (roomType.getMaxOccupancy() != null &&
                req.getGuests() > roomType.getMaxOccupancy()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Guests exceed max occupancy"
            );
        }

        long totalRooms = roomRepo.countByRoomType(roomType);
        long reservedRooms =
                reservationRepo.countOverlappingReservations(
                        roomType,
                        req.getCheckInDate(),
                        req.getCheckOutDate()
                );

        if (reservedRooms >= totalRooms) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No availability for selected dates"
            );
        }

        RatePlan ratePlan = null;
        if (req.getRatePlanId() != null) {
            ratePlan = ratePlanRepo.findById(req.getRatePlanId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Rate plan not found"));
        }

        BigDecimal totalPrice =
                pricingEngine.calculateTotalWithRatePlan(
                        roomType,
                        req.getCheckInDate(),
                        req.getCheckOutDate(),
                        ratePlan
                );

        Reservation reservation = Reservation.builder()
                .reservationCode(generateReservationCode())
                .user(user)
                .roomType(roomType)
                .checkInDate(req.getCheckInDate())
                .checkOutDate(req.getCheckOutDate())
                .numberOfGuests(req.getGuests())
                .ratePlan(ratePlan)
                .status(ReservationStatus.PENDING)
                .totalPrice(totalPrice)
                .createdAt(Instant.now())
                .build();

        Reservation saved = reservationRepo.save(reservation);

        return new ReservationResponse(
                saved.getId(),
                saved.getReservationCode(),
                saved.getUser().getId(),
                saved.getRoomType().getId(),
                saved.getStatus().name(),
                saved.getTotalPrice()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> listReservationsForUser(Long userId) {

        userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return reservationRepo.findByUserId(userId);
    }

    @Override
    public Reservation checkIn(Long reservationId) {

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        if (!paymentService.isReservationPaid(reservationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment not SUCCESS. Cannot check in"
            );
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation must be CONFIRMED to check in"
            );
        }

        List<Room> availableRooms =
                roomRepo.findByRoomTypeAndStatus(
                        reservation.getRoomType(),
                        RoomStatus.AVAILABLE
                );

        if (availableRooms.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No available rooms"
            );
        }

        Room assignedRoom = availableRooms.get(0);
        assignedRoom.setStatus(RoomStatus.OCCUPIED);
        roomRepo.save(assignedRoom);

        reservation.setRoom(assignedRoom);
        reservation.setStatus(ReservationStatus.CHECKED_IN);

        return reservationRepo.save(reservation);
    }

    @Override
    public Reservation checkOut(Long reservationId) {

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation must be CHECKED_IN to check out"
            );
        }

        Room room = reservation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepo.save(room);
            cleaningTaskRepository.save(CleaningTask.builder()
                    .room(room)
                    .status(CleaningTaskStatus.PENDING)
                    .build());
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        return reservationRepo.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        LocalDate today = LocalDate.now();
        LocalDate checkInDate = reservation.getCheckInDate();

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Reservation is already cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No-show reservations cannot be cancelled");
        }

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN ||
                reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Checked-in or checked-out reservations cannot be cancelled"
            );
        }

        if (reservation.getStatus() != ReservationStatus.PENDING &&
                reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PENDING or CONFIRMED reservations can be cancelled"
            );
        }

        if (today.isAfter(checkInDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Past check-in date reservations cannot be cancelled. Mark as NO_SHOW instead"
            );
        }

        boolean fullRefundAllowed = today.isBefore(checkInDate.minusDays(1));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED && fullRefundAllowed) {
            paymentService.refundReservation(reservationId);
        }

        Room room = reservation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepo.save(room);
            reservation.setRoom(null);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepo.save(reservation);
    }

    @Override
    public Reservation markNoShow(Long reservationId) {

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        LocalDate today = LocalDate.now();

        if (reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Reservation is already marked as NO_SHOW");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cancelled reservations cannot be marked as NO_SHOW");
        }

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN ||
                reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Checked-in or checked-out reservations cannot be marked as NO_SHOW");
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only CONFIRMED reservations can be marked as NO_SHOW");
        }

        if (!today.isAfter(reservation.getCheckInDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "NO_SHOW can only be marked after the check-in date has passed"
            );
        }

        Room room = reservation.getRoom();
        if (room != null) {
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepo.save(room);
            reservation.setRoom(null);
        }

        reservation.setStatus(ReservationStatus.NO_SHOW);
        return reservationRepo.save(reservation);
    }

    @Override
    public RoomReassignmentResponse changeRoom(Long reservationId, Long newRoomId) {

        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only CHECKED_IN reservations can change rooms"
            );
        }

        Room currentRoom = reservation.getRoom();
        if (currentRoom == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No room is currently assigned to this reservation"
            );
        }

        Room newRoom = roomRepo.findById(newRoomId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Target room not found"
                ));

        if (currentRoom.getId().equals(newRoom.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation is already assigned to this room"
            );
        }

        if (newRoom.getStatus() != RoomStatus.AVAILABLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Target room is not available for reassignment"
            );
        }

        if (reservation.getRoomType() == null || newRoom.getRoomType() == null ||
                !reservation.getRoomType().getId().equals(newRoom.getRoomType().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target room must belong to the same room type as the reservation"
            );
        }

        currentRoom.setStatus(RoomStatus.AVAILABLE);
        roomRepo.save(currentRoom);

        newRoom.setStatus(RoomStatus.OCCUPIED);
        roomRepo.save(newRoom);

        reservation.setRoom(newRoom);
        Reservation savedReservation = reservationRepo.save(reservation);

        return new RoomReassignmentResponse(
                savedReservation.getId(),
                savedReservation.getReservationCode(),
                currentRoom.getId(),
                currentRoom.getRoomNumber(),
                newRoom.getId(),
                newRoom.getRoomNumber(),
                savedReservation.getStatus().name(),
                savedReservation.getTotalPrice(),
                "Room reassignment completed successfully"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReservationViewResponse> getAllReservationsForAdmin() {
        return reservationRepo.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::mapToAdminViewResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReservationViewResponse getReservationDetailsForAdmin(Long reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        return mapToAdminViewResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminReservationViewResponse searchReservationByCode(String reservationCode) {
        Reservation reservation = reservationRepo.findByReservationCode(reservationCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found for code: " + reservationCode));

        return mapToAdminViewResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReservationViewResponse> filterReservationsForAdmin(
            ReservationStatus status,
            Long userId,
            Long roomTypeId,
            LocalDate checkInDate
    ) {
        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and(ReservationRepository.hasStatus(status));
        }
        if (userId != null) {
            spec = spec.and(ReservationRepository.hasUserId(userId));
        }
        if (roomTypeId != null) {
            spec = spec.and(ReservationRepository.hasRoomTypeId(roomTypeId));
        }
        if (checkInDate != null) {
            spec = spec.and(ReservationRepository.hasCheckInDate(checkInDate));
        }

        return reservationRepo.findAll(spec)
                .stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::mapToAdminViewResponse)
                .toList();
    }
    private AdminReservationViewResponse mapToAdminViewResponse(Reservation r) {
        return new AdminReservationViewResponse(
                r.getId(),
                r.getReservationCode(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getUser() != null ? r.getUser().getFullName() : null,
                r.getRoomType() != null ? r.getRoomType().getId() : null,
                r.getRoomType() != null ? r.getRoomType().getName() : null,
                r.getRoom() != null ? r.getRoom().getId() : null,
                r.getRoom() != null ? r.getRoom().getRoomNumber() : null,
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getNumberOfGuests(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getTotalPrice(),
                r.getCreatedAt()
        );
    }

    private String generateReservationCode() {
        String datePart = DateTimeFormatter
                .ofPattern("yyyyMMdd", Locale.US)
                .format(LocalDate.now());

        long seq = codeCounter.incrementAndGet() % 1_000_000;
        return String.format("HR-%s-%06d", datePart, seq);
    }
}
