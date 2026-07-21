# E-Commerce Simulation API

A layered-architecture REST API simulating the core flows of an e-commerce platform:
user authentication, product catalog, shopping cart, and order checkout.

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Auth | Spring Security + JWT (jjwt) |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |
| Build | Maven |

## Architecture

```
Controller  ->  Service  ->  Repository  ->  Database
     |            |
   DTO      Business logic
```

- **Controller** — HTTP only, takes/returns DTOs, no business logic.
- **Service** — all business logic and transaction boundaries (`@Transactional`).
- **Repository** — Spring Data JPA interfaces.
- **Entity** — never leaves the service layer; controllers only ever see DTOs.
- **Mapper** — manual entity ↔ DTO conversion.

## Prerequisites

- Java 21
- Docker Desktop (for local PostgreSQL and for Testcontainers-based integration tests)

## Setup

1. Clone the repository and start a local PostgreSQL instance:

   ```bash
   docker compose up -d
   ```

   This starts a `postgres:16-alpine` container with a database matching the
   application's defaults (`ecommerce_db` / `ecommerce_user` / `ecommerce_pass`),
   with data persisted in a named Docker volume.

2. Provide a `JWT_SECRET` environment variable — the application refuses to
   start without one (there is intentionally no default, see `application.yaml`).

   ```bash
   export JWT_SECRET=$(openssl rand -base64 64)
   ```

   Copy `.env.example` to `.env` for a reference of every variable the app reads
   (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`).
   Everything except `JWT_SECRET` has a sane local-dev default matching
   `docker-compose.yml`.

   If you run the app from an IDE instead of the terminal, set `JWT_SECRET` in
   your Run/Debug Configuration's environment variables — a value exported in a
   terminal is not visible to a separately-launched IDE process.

## Running the application

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`. Interactive API docs (with a
JWT "Authorize" button):

```
http://localhost:8080/swagger-ui/index.html
```

### Running the authentication UI

With the API running on port `8080`, start the small React test UI:

```bash
cd ../ecommerce-simulation-web
npm install
npm run dev
```

Open `http://localhost:3000`. The UI covers register, login, logout, forgot/reset
password, change password, and current-user status. To use a different API URL,
copy `../ecommerce-simulation-web/.env.example` to
`../ecommerce-simulation-web/.env` and change `VITE_API_URL`.

## Running the tests

```bash
./mvnw verify
```

This runs both:
- **Unit tests** (`*Test.java`, via Surefire) — service-layer logic with mocked
  repositories (Mockito + AssertJ).
- **Integration tests** (`*IT.java`, via Failsafe) — full Spring context against
  a real, ephemeral PostgreSQL container (Testcontainers), covering controllers,
  security, and end-to-end transactional behavior.

`./mvnw test` alone only runs the unit tests.

## API Overview

All endpoints are under `/api`. Full request/response schemas are in Swagger UI.

### Auth (public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register and create a secure cookie session |
| POST | `/api/auth/login` | Log in and create a secure cookie session |
| POST | `/api/auth/refresh` | Rotate refresh token and renew the session |
| POST | `/api/auth/logout` | Revoke current session and tokens |
| POST | `/api/auth/logout-all` | Revoke all sessions for the user |
| POST | `/api/auth/forgot-password` | Send a password reset email |
| POST | `/api/auth/reset-password` | Reset password; last 3 passwords cannot be reused |
| GET | `/api/auth/me` | Return the current authenticated user |
| POST | `/api/auth/change-password` | Change password and revoke every session |

Authentication tokens are returned as `HttpOnly` cookies, never in the JSON body.
The access token is short-lived; the refresh token is rotated on every refresh and
tracked by a hashed server-side session. Browser clients must first call
`GET /api/auth/csrf`, then send the `ECOMMERCE-XSRF-TOKEN` cookie value as the
`X-XSRF-TOKEN` header for state-changing requests. Cross-origin clients must use
credentials (`credentials: "include"` / Axios `withCredentials: true`).

### Testing the secure auth flow in Swagger UI

Swagger UI runs on the API origin, so the browser stores and sends the HttpOnly
authentication cookies automatically. Use this order:

1. Execute `GET /api/auth/csrf` once.
2. Execute `POST /api/auth/register` or `POST /api/auth/login`.
3. Call protected cart/order endpoints directly; do not copy a token and do not
   use the Authorize button for the normal cookie flow.
4. Execute `POST /api/auth/refresh` to rotate the token pair.
5. Execute `POST /api/auth/logout`, then verify that protected endpoints fail.

Springdoc's Swagger UI CSRF support is enabled and automatically copies the
`ECOMMERCE-XSRF-TOKEN` cookie into the `X-XSRF-TOKEN` request header.

### Categories
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/categories` | Public |
| GET | `/api/categories/{id}` | Public |
| POST | `/api/categories` | ADMIN |
| PUT | `/api/categories/{id}` | ADMIN |
| DELETE | `/api/categories/{id}` | ADMIN |

### Products
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/products?page=&size=&sort=&categoryId=&search=` | Public |
| GET | `/api/products/{id}` | Public |
| POST | `/api/products` | ADMIN |
| PUT | `/api/products/{id}` | ADMIN |
| DELETE | `/api/products/{id}` | ADMIN |

### Cart (own cart only)
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/cart` | Authenticated |
| POST | `/api/cart/items` | Authenticated |
| PUT | `/api/cart/items/{itemId}` | Authenticated |
| DELETE | `/api/cart/items/{itemId}` | Authenticated |
| DELETE | `/api/cart` | Authenticated |

### Orders
| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/orders` | Authenticated — checkout from the current cart |
| GET | `/api/orders` | Authenticated — own orders only |
| GET | `/api/orders/{id}` | Authenticated — own order only |
| PUT | `/api/orders/{id}/status` | ADMIN |
| POST | `/api/orders/{id}/cancel` | Authenticated — own order only, restores stock |

### Checkout guarantees

`POST /api/orders` runs entirely inside one `@Transactional` method:
1. Every cart line's stock is validated **before** anything is mutated
   (fail-fast, all-or-nothing — a single out-of-stock item can never leave a
   half-created order behind).
2. The order and its line items are created with **snapshotted unit prices** —
   a later product price change never affects an already-placed order.
3. Stock is decremented and the cart is cleared.

If any unexpected error occurs mid-flow, the transaction rolls back completely.

## Error responses

All errors share a consistent JSON shape:

```json
{
  "timestamp": "2026-07-09T16:15:55.098",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: '999'",
  "path": "/api/products/999",
  "fieldErrors": null
}
```

`fieldErrors` is populated only for request validation failures (400), mapping
field name to the specific validation message.
