package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.PaymentMapper;
import com.hotel.hotelreservation.dto.PaymentResponseDTO;
import com.hotel.hotelreservation.model.Payment;
import com.hotel.hotelreservation.model.PaymentMethod;
import com.hotel.hotelreservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @RequestParam Long reservationId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod paymentMethod) {

        return ResponseEntity.ok(
                PaymentMapper.toDTO(
                        paymentService.initiatePayment(reservationId, amount, paymentMethod)
                )
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDTO> confirmPayment(
            @RequestParam String transactionRef) {

        Payment payment = paymentService.confirmPayment(transactionRef);
        return ResponseEntity.ok(PaymentMapper.toDTO(payment));
    }



    @PostMapping("/fail")
    public ResponseEntity<PaymentResponseDTO> failPayment(
            @RequestParam String transactionRef) {

        return ResponseEntity.ok(
                PaymentMapper.toDTO(paymentService.failPayment(transactionRef))
        );
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentResponseDTO> refundPayment(@RequestParam Long reservationId) {
        return ResponseEntity.ok(
                PaymentMapper.toDTO(paymentService.refundReservation(reservationId))
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByReservation(
            @PathVariable Long reservationId) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByReservation(reservationId)
                        .stream()
                        .map(PaymentMapper::toDTO)
                        .toList()
        );
    }


}

