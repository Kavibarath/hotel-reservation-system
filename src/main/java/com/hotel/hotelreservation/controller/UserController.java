package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.AuthDtos.AuthResponse;
import com.hotel.hotelreservation.model.User;
import com.hotel.hotelreservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public AuthResponse getProfile(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!canAccess(user, authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this profile");
        }

        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(), user.getRole(), null);
    }

    private boolean canAccess(User user, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        boolean elevated = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") ||
                        authority.getAuthority().equals("ROLE_STAFF"));
        if (elevated) {
            return true;
        }
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> claims && claims.get("userId") instanceof Number userId) {
            return user.getId().equals(userId.longValue());
        }
        return user.getEmail().equals(authentication.getName());
    }
}
