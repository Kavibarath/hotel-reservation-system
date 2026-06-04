package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.OperationsDtos.DailyRevenueResponse;
import com.hotel.hotelreservation.dto.OperationsDtos.DashboardSummaryResponse;
import com.hotel.hotelreservation.model.PaymentStatus;
import com.hotel.hotelreservation.model.ReservationStatus;
import com.hotel.hotelreservation.model.RoomStatus;
import com.hotel.hotelreservation.repository.PaymentRepository;
import com.hotel.hotelreservation.repository.ReservationRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingController {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse dashboardSummary() {
        BigDecimal revenue = paymentRepository.findByStatus(PaymentStatus.SUCCESS).stream()
                .map(payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                reservationRepository.count(),
                reservationRepository.countByStatusIn(List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN)),
                roomRepository.countByStatus(RoomStatus.AVAILABLE),
                roomRepository.countByStatus(RoomStatus.OCCUPIED),
                roomRepository.countByStatus(RoomStatus.MAINTENANCE),
                revenue
        );
    }

    @GetMapping("/reports/daily-revenue")
    public List<DailyRevenueResponse> dailyRevenue() {
        Map<java.time.LocalDate, BigDecimal> revenueByDate = paymentRepository.findByStatus(PaymentStatus.SUCCESS).stream()
                .collect(Collectors.groupingBy(
                        payment -> payment.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO,
                                payment -> payment.getAmount() == null ? BigDecimal.ZERO : payment.getAmount(),
                                BigDecimal::add)
                ));

        return revenueByDate.entrySet().stream()
                .map(entry -> new DailyRevenueResponse(entry.getKey(), entry.getValue()))
                .toList();
    }
}
