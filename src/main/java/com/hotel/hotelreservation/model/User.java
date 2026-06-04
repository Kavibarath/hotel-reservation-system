package com.hotel.hotelreservation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Application user (guest, staff, manager).
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    /**
     * Stored as bcrypt (or other) encoded password.
     */
    @Column(nullable = false)
    private String password;

    @Column(length = 150)
    private String fullName;

    @Column(length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.GUEST;

    @Column(nullable = false)
    @Builder.Default
    private Integer loyaltyPoints = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
