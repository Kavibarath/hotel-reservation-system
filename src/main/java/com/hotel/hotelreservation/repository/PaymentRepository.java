package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.Payment;
import com.hotel.hotelreservation.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payment repository for lookup and reporting.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by transaction reference (useful to reconcile with provider).
     */
    Optional<Payment> findByTransactionRef(String transactionRef);

    boolean existsByReservationIdAndStatus(Long reservationId, PaymentStatus status);

    /**
     * Find payments by reservation id.
     */
    List<Payment> findByReservationId(Long reservationId);

    List<Payment> findByStatus(PaymentStatus status);
}
