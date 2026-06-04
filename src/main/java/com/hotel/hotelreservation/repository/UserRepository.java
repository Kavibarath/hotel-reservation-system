package com.hotel.hotelreservation.repository;

import com.hotel.hotelreservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Basic repository for application users (guests, staff).
 * Exposes finder by email, used by authentication layer.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (username for authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check existence by email (useful for registration flow).
     */
    boolean existsByEmail(String email);
}
