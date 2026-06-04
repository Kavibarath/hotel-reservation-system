package com.hotel.hotelreservation.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Describes the rate plan (Room Only, Bed & Breakfast, Half Board, etc.)
 * priceModifier is an absolute amount added per reservation for simplicity.
 * Later you may switch to percentage-based modifiers.
 */
@Entity
@Table(name = "rate_plans", indexes = {
        @Index(name = "idx_rateplan_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RatePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Absolute amount added to the total (for simplicity). Use BigDecimal to avoid FP issues.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal priceModifier = BigDecimal.ZERO;

    @Column(length = 2000)
    private String cancellationPolicy;
}
