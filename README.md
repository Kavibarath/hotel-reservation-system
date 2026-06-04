# Aurelia Grand — Hotel Reservation System

A complete, production-ready hotel reservation web application featuring a luxury five-star hotel website with end-to-end functionality including guest registration, room booking, payment processing, and comprehensive admin management.

**Status**: Production Ready | All E2E tests passing

---

## Overview

Aurelia Grand is a full-stack hotel management system that handles the complete reservation lifecycle from booking to check-out. It features a luxury hotel website (inspired by Kingsbury Colombo) on the frontend and a robust Spring Boot REST API on the backend.

---

## Screenshots

### Guest Experience

#### Home Page
![Home Page](docs/screenshots/home.png)
*Luxury hero section with elegant five-star branding*

#### Rooms & Suites
![Rooms Page](docs/screenshots/rooms.png)
*Browse rooms with detailed amenities and pricing*

#### Booking Page
![Booking Page](docs/screenshots/booking.png)
*Real-time availability search and reservation booking*

#### Login & Registration
| Login | Register |
|-------|----------|
| ![Login](docs/screenshots/login.png) | ![Register](docs/screenshots/register.png) |

#### My Reservations
![My Reservations](docs/screenshots/my-reservations.png)
*Track all your bookings with status indicators*

#### Payment Processing
![Payment](docs/screenshots/payment.png)
*Secure payment flow with transaction confirmation*

### Admin Dashboard

#### Dashboard Overview
![Admin Dashboard](docs/screenshots/admin-dashboard.png)
*Real-time stats: revenue, reservations, occupancy*

#### Reservations Management
![Admin Reservations](docs/screenshots/admin-reservations.png)
*Full reservation lifecycle management with check-in/out actions*

#### Rooms Management
![Rooms Management](docs/screenshots/admin-rooms.png)
*CRUD operations for room inventory with maintenance toggle*

#### Rate Plans
![Rate Plans](docs/screenshots/admin-rate-plans.png)
*Configure dynamic pricing and cancellation policies*

> **Note**: Screenshots are stored in `docs/screenshots/`. See [screenshot guide](docs/screenshots/README.md) for naming conventions.

---

## Features

### Guest Features
- **User Registration & Authentication** — Secure JWT-based authentication with role-based access control
- **Luxury Room Browsing** — Browse rooms with high-quality images and detailed amenities
- **Real-time Availability Search** — Check room availability by date range
- **Booking System** — Create reservations with instant confirmation
- **Payment Processing** — Secure payment flow with transaction references
- **My Reservations** — Track all bookings, view details, and cancel if eligible
- **Responsive Design** — Mobile-friendly luxury aesthetic

### Admin Features
- **Reservation Management** — View, search, and filter all bookings
- **Check-in / Check-out** — Guest check-in with payment verification
- **Room Management** — Create, edit, delete rooms; toggle maintenance status
- **Rate Plan Configuration** — Dynamic pricing and cancellation policies
- **Room Reassignment** — Change guest rooms during their stay
- **Dashboard & Reports** — Revenue tracking, daily summaries, occupancy stats
- **Cleaning Tasks** — Auto-generated on check-out
- **Maintenance Requests** — Track maintenance issues
- **No-Show Handling** — Mark guests as no-show after check-in date

---

## Tech Stack

### Backend
| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 3.5.7 |
| Language | Java 17 |
| Security | Spring Security + JWT |
| Database | MySQL 8.0 with JPA/Hibernate |
| Build Tool | Gradle |
| Server Port | 8080 |

### Frontend
| Component | Technology |
|-----------|-----------|
| Framework | React 19 |
| Build Tool | Vite |
| Routing | React Router v6 |
| HTTP Client | Fetch API |
| Styling | Custom CSS (glass-morphism) |
| Icons | Lucide React |
| Fonts | Cormorant Garamond |
| Server Port | 5173 |

---

## Installation & Setup

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- MySQL 8.0 or higher
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/Kavibarath/hotel-reservation-system.git
cd hotel-reservation-system
```

### 2. Database Setup

```sql
CREATE DATABASE hotel_reservation;
```

### 3. Configure Backend

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation
spring.datasource.username=root
spring.datasource.password=your_password

jwt.secret=your-secret-key-minimum-32-characters-long
jwt.expiration=3600000

server.port=8080
```

### 4. Run Backend

```bash
# Using Gradle wrapper
./gradlew bootRun

# Or build and run JAR
./gradlew build
java -jar build/libs/hotel-reservation-0.0.1-SNAPSHOT.jar
```

Backend runs on: **http://localhost:8080**

### 5. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: **http://localhost:5173**

---

## Project Structure

```
hotel-reservation-system/
├── src/
│   ├── main/
│   │   ├── java/com/hotel/hotelreservation/
│   │   │   ├── config/              # Security, JWT, CORS configuration
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── exception/           # Global error handling
│   │   │   ├── model/               # JPA entities
│   │   │   ├── repository/          # Data access layer
│   │   │   └── service/             # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/                        # Unit & integration tests
├── frontend/
│   ├── src/
│   │   ├── components/              # Reusable React components
│   │   ├── pages/                   # Page-level components
│   │   │   └── admin/               # Admin dashboard pages
│   │   ├── context/                 # React context providers
│   │   ├── App.jsx
│   │   ├── App.css                  # Luxury styling
│   │   └── main.jsx
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── build.gradle
├── settings.gradle
├── .gitignore
└── README.md
```

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/room-types` | List all room types |
| GET | `/api/rate-plans` | List available rate plans |
| POST | `/api/search-availability` | Search available rooms |

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | User login (returns JWT) |
| GET | `/api/users/profile` | Get current user profile |

### Reservations (Guest)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reservations` | Create a new reservation |
| GET | `/api/reservations/my` | Get my reservations |
| GET | `/api/reservations/{id}` | Get reservation details |
| POST | `/api/reservations/{id}/cancel` | Cancel reservation |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/initiate` | Initiate payment |
| POST | `/api/payments/confirm` | Confirm payment |
| GET | `/api/payments/reservation/{id}` | Get payment details |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/reservations` | List all reservations |
| GET | `/api/admin/reservations/search` | Search by reservation code |
| GET | `/api/admin/reservations/filter` | Filter by status |
| POST | `/api/admin/reservations/{id}/check-in` | Check in guest |
| POST | `/api/admin/reservations/{id}/check-out` | Check out guest |
| POST | `/api/admin/reservations/{id}/change-room` | Reassign room |
| POST | `/api/admin/reservations/{id}/no-show` | Mark as no-show |
| GET | `/api/admin/catalog/rooms` | List all rooms |
| POST | `/api/admin/catalog/rooms` | Create a room |
| PUT | `/api/admin/catalog/rooms/{id}` | Update a room |
| DELETE | `/api/admin/catalog/rooms/{id}` | Delete a room |
| POST | `/api/admin/rooms/{id}/mark-maintenance` | Mark room maintenance |
| POST | `/api/admin/rooms/{id}/mark-available` | Mark room available |
| GET | `/api/admin/catalog/rate-plans` | List rate plans |
| POST | `/api/admin/catalog/rate-plans` | Create rate plan |
| PUT | `/api/admin/catalog/rate-plans/{id}` | Update rate plan |
| DELETE | `/api/admin/catalog/rate-plans/{id}` | Delete rate plan |
| GET | `/api/admin/dashboard/summary` | Dashboard statistics |
| GET | `/api/admin/reports/daily-revenue` | Daily revenue report |
| GET | `/api/admin/cleaning-tasks` | List cleaning tasks |
| GET | `/api/admin/maintenance-requests` | List maintenance requests |

---

## Reservation Lifecycle

```
   [User Creates Booking]
            |
            v
       PENDING (unpaid)
            |
            v
   [Completes Payment]
            |
            v
      CONFIRMED (paid)
            |
            v
   [Check-in Date Arrives]
            |
            v
     CHECKED_IN (occupied)
            |
            v
       [Check-out]
            |
            v
      CHECKED_OUT (complete)

  Alternative Flows:
  - Cancel anytime  -> CANCELLED (refund issued)
  - No-show after date -> NO_SHOW
```

---

## Database Schema

### Key Entities

- **User** — Guest and staff accounts with role-based access (GUEST, STAFF, ADMIN)
- **RoomType** — Suite categories (Deluxe, Executive Suite, etc.)
- **Room** — Physical rooms with status tracking (AVAILABLE, OCCUPIED, MAINTENANCE)
- **Reservation** — Booking with full lifecycle status
- **Payment** — Transaction records with refund handling
- **RatePlan** — Dynamic pricing configurations
- **CleaningTask** — Auto-generated housekeeping assignments
- **MaintenanceRequest** — Guest-reported maintenance issues

---

## Security

- **JWT Authentication** — Stateless token-based auth
- **Role-Based Access Control** — GUEST, STAFF, ADMIN roles
- **Password Hashing** — BCrypt encryption
- **CORS Configuration** — Configured for cross-origin requests
- **Input Validation** — Server-side validation on all endpoints
- **Protected Routes** — Admin endpoints require ADMIN role

---

## Design Highlights

- **Luxury Aesthetic** — Cormorant Garamond serif fonts with gold accents
- **Glass-Morphism** — Frosted glass effects on dark backgrounds
- **Gradient Cards** — Distinct color palettes for amenity cards
- **Scroll-Responsive Navbar** — Dynamic styling on scroll
- **Mobile-First Design** — Responsive across all devices
- **Accessibility** — Semantic HTML and keyboard navigation

---

## Testing

### Tested Flows
- Public API endpoints (room types, rate plans, availability)
- Authentication (register, login, JWT, role checking)
- Booking workflow (create, pay, confirm, status transitions)
- Admin operations (check-in/out, room changes, search, filter)
- Cancel & refund flow
- CRUD operations (rooms, rate plans)
- Housekeeping (cleaning tasks, maintenance requests)
- Validation (dates, occupancy, permissions)
- Security (authentication, authorization, CORS)

---

## Deployment

### Backend
```bash
./gradlew build
# Deploy build/libs/hotel-reservation-0.0.1-SNAPSHOT.jar to your server
# Configure production database in environment variables
```

### Frontend
```bash
cd frontend
npm run build
# Deploy the dist/ folder to your CDN or web server
# Update API endpoint to production backend URL
```

---

## Troubleshooting

**Port 8080 already in use**
- Change `server.port` in `application.properties`
- Update frontend proxy in `vite.config.js`

**CORS errors**
- Ensure `SecurityConfig.java` has proper CORS configuration
- Verify frontend origin is in the allowed origins

**Database connection failed**
- Confirm MySQL is running
- Verify credentials in `application.properties`
- Ensure database exists: `CREATE DATABASE hotel_reservation;`

**Frontend can't connect to API**
- Confirm backend is running on port 8080
- Check Vite proxy configuration

---

## Author

**Sitharthan Kavibarath**  
GitHub: [@Kavibarath](https://github.com/Kavibarath)

---

## License

This project is for educational and personal use.

---

**Last Updated**: June 2026
