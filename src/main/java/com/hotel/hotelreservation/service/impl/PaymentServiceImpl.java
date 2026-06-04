package com.hotel.hotelreservation.service.impl;

import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.PaymentRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public Payment initiatePayment(Long reservationId,
                                   BigDecimal amount,
                                   PaymentMethod paymentMethod) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment amount must be greater than zero");
        }

        if (paymentMethod == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment method is required");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only PENDING reservations can be paid");
        }

        if (reservation.getTotalPrice() == null ||
                amount.compareTo(reservation.getTotalPrice()) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment amount must match reservation total");
        }

        if (isReservationPaid(reservationId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Reservation is already paid");
        }

        boolean pendingPaymentExists = paymentRepository
                .existsByReservationIdAndStatus(reservationId, PaymentStatus.PENDING);

        if (pendingPaymentExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A pending payment already exists for this reservation");
        }

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PENDING)
                .transactionRef(generateTransactionRef())
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    public Payment confirmPayment(String transactionRef) {

        String cleanRef = transactionRef.trim();

        Payment payment = paymentRepository.findByTransactionRef(cleanRef)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found for ref: " + cleanRef
                ));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Refunded payment cannot be confirmed again");
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Failed payment cannot be confirmed");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        }

        Reservation reservation = payment.getReservation();

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
        }

        return payment;
    }

    @Override
    public Payment failPayment(String transactionRef) {

        String cleanRef = transactionRef.trim();

        Payment payment = paymentRepository.findByTransactionRef(cleanRef)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found for ref: " + cleanRef
                ));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Successful payment cannot be failed");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Refunded payment cannot be failed");
        }

        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReservationPaid(Long reservationId) {
        return paymentRepository.existsByReservationIdAndStatus(reservationId, PaymentStatus.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByReservation(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
    }

    @Override
    public Payment refundReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found"));

        List<Payment> payments = paymentRepository.findByReservationId(reservation.getId());

        boolean alreadyRefunded = payments.stream()
                .anyMatch(payment -> payment.getStatus() == PaymentStatus.REFUNDED);

        if (alreadyRefunded) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Reservation payment is already refunded");
        }

        Payment successPayment = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No successful payment found to refund"
                ));

        successPayment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(successPayment);
    }

    private String generateTransactionRef() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
