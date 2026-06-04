package com.hotel.hotelreservation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class HotelReservationApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String guestToken() throws Exception {
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "guest@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(loginJson).get("token").asText();
    }

    private String adminToken() throws Exception {
        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "manager@example.com",
                                  "password": "Admin123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(loginJson).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void reservationPaymentCheckInAndCheckOutFlow() throws Exception {
        String guestToken = guestToken();
        String adminToken = adminToken();

        mockMvc.perform(post("/api/search-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "checkInDate": "2026-08-01",
                                  "checkOutDate": "2026-08-03",
                                  "guests": 2,
                                  "roomTypeId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].totalPrice").value(300.00));

        String reservationJson = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "roomTypeId": 1,
                                  "checkInDate": "2026-08-01",
                                  "checkOutDate": "2026-08-03",
                                  "guests": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(300.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode reservation = objectMapper.readTree(reservationJson);
        long reservationId = reservation.get("reservationId").asLong();

        String paymentJson = mockMvc.perform(post("/api/payments/initiate")
                        .header("Authorization", bearer(guestToken))
                        .param("reservationId", String.valueOf(reservationId))
                        .param("amount", "300.00")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String transactionRef = objectMapper.readTree(paymentJson).get("transactionRef").asText();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(guestToken))
                        .param("transactionRef", transactionRef))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/admin/reservations/{id}", reservationId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(post("/api/admin/reservations/{id}/check-in", reservationId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        mockMvc.perform(post("/api/admin/reservations/{id}/check-out", reservationId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", bearer(guestToken))
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    void paymentAmountMustMatchReservationTotal() throws Exception {
        String guestToken = guestToken();

        String reservationJson = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "roomTypeId": 2,
                                  "checkInDate": "2026-09-01",
                                  "checkOutDate": "2026-09-02",
                                  "guests": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reservationId = objectMapper.readTree(reservationJson).get("reservationId").asLong();

        mockMvc.perform(post("/api/payments/initiate")
                        .header("Authorization", bearer(guestToken))
                        .param("reservationId", String.valueOf(reservationId))
                        .param("amount", "1.00")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment amount must match reservation total"));
    }

    @Test
    void guestCanRegisterAndLogin() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.guest@example.com",
                                  "password": "secret123",
                                  "fullName": "New Guest",
                                  "phone": "0772223333"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.guest@example.com"))
                .andExpect(jsonPath("$.role").value("GUEST"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.guest@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New Guest"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.guest@example.com",
                                  "password": "secret123",
                                  "fullName": "Duplicate Guest"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void adminCanManageRoomsAndPackages() throws Exception {
        String adminToken = adminToken();

        String roomTypeJson = mockMvc.perform(post("/api/admin/catalog/room-types")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Presidential Pavilion",
                                  "description": "Private event-ready suite",
                                  "maxOccupancy": 4,
                                  "bedType": "King",
                                  "sizeSqm": 120,
                                  "basePricePerNight": 650.00,
                                  "amenities": "Butler,Private Dining",
                                  "images": "pavilion.jpg"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Presidential Pavilion"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long roomTypeId = objectMapper.readTree(roomTypeJson).get("id").asLong();

        String roomJson = mockMvc.perform(post("/api/admin/catalog/rooms")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomNumber": "901",
                                  "floor": 9,
                                  "roomTypeId": %d,
                                  "status": "AVAILABLE"
                                }
                                """.formatted(roomTypeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("901"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long roomId = objectMapper.readTree(roomJson).get("id").asLong();

        mockMvc.perform(put("/api/admin/catalog/rooms/{id}", roomId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomNumber": "901A",
                                  "floor": 9,
                                  "roomTypeId": %d,
                                  "status": "OUT_OF_SERVICE"
                                }
                                """.formatted(roomTypeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"));

        mockMvc.perform(delete("/api/admin/catalog/room-types/{id}", roomTypeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room type is in use and cannot be deleted"));

        mockMvc.perform(delete("/api/admin/catalog/rooms/{id}", roomId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/admin/catalog/room-types/{id}", roomTypeId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());

        String ratePlanJson = mockMvc.perform(post("/api/admin/catalog/rate-plans")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Wedding Half Board",
                                  "description": "Breakfast and dinner for event guests",
                                  "priceModifier": 85.00,
                                  "cancellationPolicy": "Free cancellation before 7 days"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Wedding Half Board"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long ratePlanId = objectMapper.readTree(ratePlanJson).get("id").asLong();

        mockMvc.perform(put("/api/admin/catalog/rate-plans/{id}", ratePlanId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Wedding Full Board",
                                  "description": "All-day dining for event guests",
                                  "priceModifier": 140.00,
                                  "cancellationPolicy": "Manager approval within 7 days"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceModifier").value(140.00));

        mockMvc.perform(delete("/api/admin/catalog/rate-plans/{id}", ratePlanId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    void blueprintSecurityRoomsOperationsReportingAndRefundFlow() throws Exception {
        String guestToken = guestToken();
        String adminToken = adminToken();

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/1")
                        .header("Authorization", bearer(guestToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("guest@example.com"));

        mockMvc.perform(get("/api/rooms")
                        .header("Authorization", bearer(guestToken))
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        String roomJson = mockMvc.perform(post("/api/admin/rooms")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomNumber": "777",
                                  "floor": 7,
                                  "roomTypeId": 1,
                                  "status": "AVAILABLE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("777"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long roomId = objectMapper.readTree(roomJson).get("id").asLong();

        mockMvc.perform(post("/api/admin/rooms/{id}/mark-maintenance", roomId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        mockMvc.perform(post("/api/admin/rooms/{id}/mark-available", roomId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(post("/api/maintenance-requests")
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "createdByUserId": 1,
                                  "description": "Air conditioning is noisy"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));

        mockMvc.perform(get("/api/admin/maintenance-requests")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        String reservationJson = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "roomTypeId": 1,
                                  "checkInDate": "2026-10-01",
                                  "checkOutDate": "2026-10-03",
                                  "guests": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long reservationId = objectMapper.readTree(reservationJson).get("reservationId").asLong();

        String paymentJson = mockMvc.perform(post("/api/payments/initiate")
                        .header("Authorization", bearer(guestToken))
                        .param("reservationId", String.valueOf(reservationId))
                        .param("amount", "300.00")
                        .param("paymentMethod", "CARD"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String transactionRef = objectMapper.readTree(paymentJson).get("transactionRef").asText();

        mockMvc.perform(post("/api/payments/confirm")
                        .header("Authorization", bearer(guestToken))
                        .param("transactionRef", transactionRef))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(post("/api/admin/reservations/{id}/check-in", reservationId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        mockMvc.perform(post("/api/admin/reservations/{id}/check-out", reservationId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        mockMvc.perform(get("/api/admin/cleaning-tasks")
                        .header("Authorization", bearer(adminToken))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

        mockMvc.perform(post("/api/payments/refund")
                        .header("Authorization", bearer(guestToken))
                        .param("reservationId", String.valueOf(reservationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReservations", greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/admin/reports/daily-revenue")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }
}
