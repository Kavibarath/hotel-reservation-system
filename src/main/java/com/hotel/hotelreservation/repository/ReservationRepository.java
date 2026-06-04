package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.Reservation;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.model.RoomType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    @Query("select r from Reservation r " +
            "where r.roomType = :roomType " +
            "  and r.status in :activeStatuses " +
            "  and not (r.checkOutDate <= :start or r.checkInDate >= :end)")
    List<Reservation> findOverlappingReservations(
            @Param("roomType") RoomType roomType,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses
    );

    default List<Reservation> findOverlappingReservations(RoomType roomType, LocalDate start, LocalDate end) {
        return findOverlappingReservations(
                roomType,
                start,
                end,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN)
        );
    }

    @Query("select r from Reservation r where r.user.id = :userId order by r.checkInDate desc")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Query("select count(r) from Reservation r " +
            "where r.roomType = :roomType " +
            "  and r.status in :activeStatuses " +
            "  and not (r.checkOutDate <= :start or r.checkInDate >= :end)")
    long countOverlappingReservations(
            @Param("roomType") RoomType roomType,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses
    );

    default long countOverlappingReservations(RoomType roomType, LocalDate start, LocalDate end) {
        return countOverlappingReservations(
                roomType,
                start,
                end,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN)
        );
    }

    Optional<Reservation> findByReservationCode(String reservationCode);

    boolean existsByRoomId(Long roomId);

    boolean existsByRoomTypeId(Long roomTypeId);

    boolean existsByRatePlanId(Long ratePlanId);

    long countByStatusIn(List<ReservationStatus> statuses);

    static Specification<Reservation> hasStatus(ReservationStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    static Specification<Reservation> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    static Specification<Reservation> hasRoomTypeId(Long roomTypeId) {
        return (root, query, cb) ->
                roomTypeId == null ? null : cb.equal(root.get("roomType").get("id"), roomTypeId);
    }

    static Specification<Reservation> hasCheckInDate(LocalDate checkInDate) {
        return (root, query, cb) ->
                checkInDate == null ? null : cb.equal(root.get("checkInDate"), checkInDate);
    }
}
