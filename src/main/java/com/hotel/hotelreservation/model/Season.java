package com.hotel.hotelreservation.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Season definition (peak, off-peak, weekend).
 * Use BigDecimal for multiplier to keep exact values (and allow precision/scale).
 */
@Entity
@Table(name = "seasons", indexes = {
        @Index(name = "idx_season_range", columnList = "start_date,end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Pricing multiplier stored as DECIMAL in DB.
     * Example: 1.25 = +25%, 0.85 = -15%
     */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal multiplier = BigDecimal.valueOf(1.00);
}
