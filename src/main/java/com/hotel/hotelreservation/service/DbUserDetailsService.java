package com.hotel.hotelreservation.service;

import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the database and maps role enum to Spring Security role.
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Build Spring Security user with role prefixed by ROLE_
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword()) // must be BCrypt encoded in DB
                .roles(normalizedRole(u.getRole().name())) // e.g. "GUEST" -> role will be ROLE_GUEST
                .build();
    }

    private String normalizedRole(String role) {
        if ("MANAGER".equals(role)) {
            return "ADMIN";
        }
        if ("RECEPTIONIST".equals(role)) {
            return "STAFF";
        }
        return role;
    }
}
