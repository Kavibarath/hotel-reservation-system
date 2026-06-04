package com.hotel.hotelreservation.dto;

import com.hotel.hotelreservation.model.Payment;

public class PaymentMapper {

    public static PaymentResponseDTO toDTO(Payment payment) {

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setTransactionRef(payment.getTransactionRef());
        dto.setCreatedAt(payment.getCreatedAt());

        return dto;
    }
}

