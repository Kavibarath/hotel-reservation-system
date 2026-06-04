package com.hotel.hotelreservation.config;

import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds initial database data for testing.
 * Runs once on application startup if tables are empty.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            RoomTypeRepository roomTypeRepo,
            RoomRepository roomRepo,
            SeasonRepository seasonRepo,
            RatePlanRepository ratePlanRepo,
            UserRepository userRepo
    ) {
        return args -> {

            // ---------------------------
            // 1) Room Types
            // ---------------------------
            if (roomTypeRepo.count() == 0) {
                RoomType deluxeKing = RoomType.builder()
                        .name("Deluxe King")
                        .description("Spacious room with a king-size bed, city view.")
                        .maxOccupancy(2)
                        .bedType("King")
                        .sizeSqm(40.0)
                        .basePricePerNight(new BigDecimal("150.00"))
                        .amenities("WiFi,TV,Mini Bar,AC")
                        .images("deluxe1.jpg,deluxe2.jpg")
                        .build();

                RoomType oceanSuite = RoomType.builder()
                        .name("Ocean Suite")
                        .description("Luxury suite with ocean view and private balcony.")
                        .maxOccupancy(3)
                        .bedType("King")
                        .sizeSqm(65.0)
                        .basePricePerNight(new BigDecimal("280.00"))
                        .amenities("WiFi,TV,Mini Bar,AC,Balcony,Jacuzzi")
                        .images("suite1.jpg,suite2.jpg")
                        .build();

                roomTypeRepo.save(deluxeKing);
                roomTypeRepo.save(oceanSuite);

                System.out.println("[SEED] Room Types created.");
            }

            // ---------------------------
            // 2) Rooms (physical rooms)
            // ---------------------------
            if (roomRepo.count() == 0) {
                RoomType deluxeKing = roomTypeRepo.findByNameIgnoreCase("Deluxe King").orElseThrow();
                RoomType oceanSuite = roomTypeRepo.findByNameIgnoreCase("Ocean Suite").orElseThrow();

                roomRepo.save(Room.builder().roomNumber("101").floor(1).roomType(deluxeKing).build());
                roomRepo.save(Room.builder().roomNumber("102").floor(1).roomType(deluxeKing).build());
                roomRepo.save(Room.builder().roomNumber("201").floor(2).roomType(oceanSuite).build());
                roomRepo.save(Room.builder().roomNumber("202").floor(2).roomType(oceanSuite).build());
                roomRepo.save(Room.builder().roomNumber("203").floor(2).roomType(oceanSuite).build());

                System.out.println("[SEED] Rooms created.");
            }

            // ---------------------------
            // 3) Seasons
            // ---------------------------
            if (seasonRepo.count() == 0) {
                seasonRepo.save(Season.builder()
                        .name("Peak Season")
                        .startDate(LocalDate.of(2025, 12, 1))
                        .endDate(LocalDate.of(2026, 1, 15))
                        .multiplier(new BigDecimal("1.50"))   // +50%
                        .build());

                seasonRepo.save(Season.builder()
                        .name("Off Peak")
                        .startDate(LocalDate.of(2025, 5, 1))
                        .endDate(LocalDate.of(2025, 6, 30))
                        .multiplier(new BigDecimal("0.85"))   // -15%
                        .build());

                seasonRepo.save(Season.builder()
                        .name("Weekend")
                        .startDate(LocalDate.of(2025, 1, 1))
                        .endDate(LocalDate.of(2025, 12, 31))
                        .multiplier(new BigDecimal("1.20"))   // +20% every Saturday/Sunday (simplified)
                        .build());

                System.out.println("[SEED] Seasons created.");
            }

            // ---------------------------
            // 4) Rate Plans
            // ---------------------------
            if (ratePlanRepo.count() == 0) {
                ratePlanRepo.save(RatePlan.builder()
                        .name("Bed & Breakfast")
                        .description("Includes buffet breakfast for all guests.")
                        .priceModifier(new BigDecimal("25.00"))
                        .cancellationPolicy("Free cancellation before 48 hours.")
                        .build());

                System.out.println("[SEED] Rate Plan created.");
            }

            // ---------------------------
            // 5) Guest User
            // ---------------------------
            if (userRepo.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                userRepo.save(User.builder()
                        .email("guest@example.com")
                        .password(encoder.encode("123456"))
                        .fullName("Test Guest")
                        .phone("0771234567")
                        .role(Role.GUEST)
                        .loyaltyPoints(50)
                        .build());

                System.out.println("[SEED] Guest User created.");
            }
            // ---------------------------
// Manager User (for admin endpoints)
// ---------------------------
            if (!userRepo.existsByEmail("manager@example.com")) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                userRepo.save(User.builder()
                        .email("manager@example.com")
                        .password(encoder.encode("Admin123!")) // store BCrypt encoded password
                        .fullName("Manager One")
                        .phone("0770000001")
                        .role(Role.ADMIN)
                        .loyaltyPoints(0)
                        .build());
                System.out.println("[SEED] Manager user created: manager@example.com / Admin123!");
            } else {
                userRepo.findByEmail("manager@example.com").ifPresent(manager -> {
                    manager.setRole(Role.ADMIN);
                    userRepo.save(manager);
                });
            }


            System.out.println("[SEED] Database seeding completed!");
        };
    }
}

