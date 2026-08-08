# StayEasy

A property rental platform built with Spring Boot, extended with a full authentication and authorization layer, a standalone REST microservice, scheduled jobs, caching, and a tested codebase across both applications.

Originally scaffolded from: https://github.com/sibell06/stayeasy-exam-may-2026.git

## Overview

StayEasy lets hosts list properties, renters browse and book them, and both sides manage reservations through a review and approval flow. Renters also earn and redeem loyalty points on their stays through a separate microservice.

The project is made up of two independent Spring Boot applications:

- **Main application** (`/`) — the Thymeleaf-based web app: property listings, bookings, reviews, user accounts, security, and admin tools.
- **loyalty-svc** (`/loyalty-svc`) — a standalone REST microservice that tracks renters' loyalty point balances, independently deployed with its own database.

## Tech Stack

- Java 17, Spring Boot 3.4
- Spring MVC, Thymeleaf
- Spring Data JPA, MySQL
- Spring Security
- Spring Cloud OpenFeign
- Spring Cache
- Spring Scheduling
- JUnit 5, Mockito, MockMvc, H2 (test database)
- JaCoCo (test coverage reporting)
- Maven

## Project Structure

```
stayeasy-advanced/
├── pom.xml                    Main application
├── src/
│   ├── main/java/com/softuni/stayeasy/
│   └── test/java/com/softuni/stayeasy/
├── loyalty-svc/                Standalone microservice
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/softuni/loyaltysvc/
│       └── test/java/com/softuni/loyaltysvc/
└── README.md
```

## Features

### Roles

Three roles, each with a different level of access:

- **RENTER** — browse properties, book stays, redeem loyalty points, leave reviews.
- **HOST** — list and manage their own properties, approve or reject reservations.
- **ADMIN** — full access across the platform, including managing other users' roles.

### Core functionality

- Property listing, browsing, editing, and deletion
- Reservation creation, approval, rejection, and cancellation
- Reviews on properties
- User registration, login, and profile management
- Admin panel for managing user roles
- Loyalty points: earned automatically when a host approves a reservation, redeemable for a discount at checkout

### Security

- Spring Security with session-based authentication
- CSRF protection enabled throughout
- Open, authenticated, and role-restricted routes
- Admins can view and change any user's role
- Users can view and edit their own profile

### Scheduling and Caching

- A daily cron job expires reservations that were never approved within 7 days
- A fixed-rate job marks approved reservations as completed once their checkout date has passed
- The list of available properties is cached, with automatic eviction on create, update, or delete

### REST Microservice (loyalty-svc)

A genuinely separate Spring Boot application with its own database, called from the main application via a Feign client:

- `GET /api/loyalty/balance/{userId}` — current points balance
- `POST /api/loyalty/award` — award points for completed nights
- `POST /api/loyalty/redeem` — redeem points for a booking discount

## Getting Started

### Prerequisites

- Java 17
- Maven
- MySQL running locally

### Database Setup

Both applications connect to their own MySQL database, created automatically on first run:

- Main application: `stayeasy`
- loyalty-svc: `loyalty_svc`

Update the credentials in each application's `src/main/resources/application.properties` if your local MySQL setup differs from the defaults (`root` / adjust as needed).

### Running the Main Application

```
mvn spring-boot:run
```

Runs on `http://localhost:8080`.

### Running loyalty-svc

```
cd loyalty-svc
mvn spring-boot:run
```

Runs on `http://localhost:8081`. The main application expects this service to be running for loyalty point features (booking discounts, points balance display) to work; if it is unavailable, those features fail gracefully rather than breaking the main flow.

### Test Accounts

On first run, the main application automatically seeds three test accounts along with two sample properties, so the app can be explored immediately without manual setup:

| Username | Password    | Role   |
|----------|-------------|--------|
| admin1   | Admin123!   | ADMIN  |
| host1    | Host123!    | HOST   |
| renter1  | Renter123!  | RENTER |

## Testing

Both applications include unit tests, integration tests, and API tests, with test coverage tracked via JaCoCo.

To run the tests and generate a coverage report:

```
mvn clean test
```

The report is generated at `target/site/jacoco/index.html` for each application.

- Main application: unit tests for the service layer, integration tests against an in-memory H2 database, and API tests for every controller using MockMvc with real Spring Security enforcement.
- loyalty-svc: unit tests for the loyalty service, integration tests for the repository layer, and API tests for the REST endpoints, including validation and error handling.

## Logging

Both applications log key domain events (reservations created and approved, properties created and deleted, points awarded and redeemed, user registration, role changes, and scheduled job runs) using SLF4J.

## Error Handling

Both applications use a centralized exception handler. Invalid input returns a validation message; missing resources and unauthorized actions return a meaningful response rather than a generic error page.
