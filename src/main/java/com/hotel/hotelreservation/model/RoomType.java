package com.hotel.hotelreservation.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Room type definition (e.g., Deluxe King, Premier Ocean View).
 * Keep amenities and images as delimited strings for now; later you can
 * extract them to separate tables if needed.
 */
@Entity
@Table(name = "room_types", indexes = {
        @Index(name = "idx_room_type_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxOccupancy = 2;

    @Column(length = 60)
    private String bedType;

    /**
     * Size in square meters (optional).
     */
    private Double sizeSqm;

    /**
     * Base price per night (before season multiplier and rate plan modifier).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal basePricePerNight = BigDecimal.ZERO;

    /**
     * Comma-separated list of amenities (simple approach).
     */
    @Column(length = 1000)
    private String amenities;

    /**
     * Comma-separated image URLs or paths.
     */
    @Column(length = 2000)
    private String images;
}
