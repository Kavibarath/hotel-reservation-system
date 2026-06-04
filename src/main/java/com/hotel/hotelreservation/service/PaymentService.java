package com.hotel.hotelreservation.service;

import com.hotel.hotelreservation.model.Payment;
import com.hotel.hotelreservation.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {

    Payment initiatePayment(Long reservationId,
                            BigDecimal amount,
                            PaymentMethod paymentMethod);

    Payment confirmPayment(String transactionRef);

    Payment failPayment(String transactionRef);

    boolean isReservationPaid(Long reservationId);

    List<Payment> getPaymentsByReservation(Long reservationId);

    Payment refundReservation(Long reservationId);
}

