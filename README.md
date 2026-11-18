# Let's Play

A secure RESTful CRUD API for a small e-commerce-like platform, built with **Spring Boot**, **MongoDB** and **Spring Security (JWT)**. Users register, log in and manage their own products; admins manage all users and products. All traffic is served over **HTTPS**.

## Table of contents

- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Authentication](#authentication)
- [API endpoints](#api-endpoints)
- [Request and response examples](#request-and-response-examples)
- [Error handling](#error-handling)
- [Security measures](#security-measures)
- [Testing](#testing)
- [Project structure](#project-structure)
- [Troubleshooting](#troubleshooting)

## Tech stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4, Spring Web, Spring Data MongoDB |
| Security | Spring Security, JWT (jjwt, HS256), BCrypt |
| Database | MongoDB (Docker) |
| Transport | HTTPS with a self-signed certificate (PKCS12 keystore) |
| Build | Maven (wrapper included) |

## Getting started

### Prerequisites

- Java 21 (`java -version`)
- Docker with Docker Compose (`docker compose version`)
- OpenSSL (to generate the JWT secret)
- Python 3, optional, to run the test and seed scripts

### 1. Clone the repository

```bash
git clone https://github.com/Marouane-EN/lets-play.git
cd lets-play
```

### 2. Create your `.env`

All secrets are read from a `.env` file in the project root. It is ignored by git; a template is provided:

```bash
cp .env.example .env
```

Then edit `.env`:

| Variable | Purpose |
|---|---|
| `MONGO_USER`, `MONGO_PASSWORD` | MongoDB root credentials, used by both Docker and the app |
| `JWT_SECRET` | Key that signs the JWTs. Generate one with `openssl rand -base64 32` |
| `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | The admin account created on first start (password: 8 to 72 characters) |
| `KEYSTORE_PASSWORD` | Password of the HTTPS keystore created in the next step (at least 6 characters) |

### 3. Generate the HTTPS certificate

The API only accepts HTTPS. Create a self-signed certificate for `localhost` in the project root; when asked for a password, use the value of `KEYSTORE_PASSWORD`:

```bash
keytool -genkeypair -alias letsplay -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 365 \
  -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"
```

Export the public certificate so clients such as `curl` can trust it:

```bash
keytool -exportcert -alias letsplay -keystore keystore.p12 -rfc -file letsplay.crt
```

`keystore.p12` contains the server's private key and is never committed (`*.p12` is in `.gitignore`).

### 4. Start MongoDB

```bash
docker compose up -d
```

### 5. Run the application

```bash
./mvnw spring-boot:run
```

Use `spring-boot:run` rather than `./mvnw package`: `package` also runs the integration test, which needs MongoDB and the `.env` values to be available.

The API is available at **`https://localhost:8443`**. On first start, the admin account from `.env` is created; the log shows `Super Admin created successfully`.

### 6. Check that it works

```bash
curl --cacert letsplay.crt https://localhost:8443/products
```

This returns `[]` on an empty database.

**About the browser warning:** the certificate is self-signed, so browsers show "Your connection is not private". This is expected for local development: the connection is encrypted, but no public certificate authority vouches for `localhost`. In **Postman**, either add `letsplay.crt` under *Settings > Certificates > CA certificates*, or turn off *SSL certificate verification* under *Settings > General*.

## Authentication

1. Register (`POST /auth/register`) or log in (`POST /auth/login`). Both return a JWT.
2. Send the token in the `Authorization` header of every protected request:

```
Authorization: Bearer <token>
```

- Tokens are valid for **24 hours** and signed with HS256.
- You can log in with either your **email or your username** (the `identifier` field).
- New accounts always get the `USER` role; the `role` field is ignored at registration. Only an admin can make another user an admin.
- The user is loaded from the database on every request, so a deleted user's token stops working immediately, and role changes apply at once.

### Roles

| Role | Permissions |
|---|---|
| `USER` | Create products, and update or delete **their own** products |
| `ADMIN` | Everything a user can do, plus manage **all** users and **all** products |

## API endpoints

### Auth

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Create an account, returns a token |
| `POST` | `/auth/login` | Public | Log in with email or username, returns a token |

### Products

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/products` | Public | List all products |
| `GET` | `/products/{id}` | Public | Get one product |
| `POST` | `/products` | Authenticated | Create a product owned by the current user |
| `PUT` | `/products/{id}` | Owner or admin | Replace a product's name, description and price |
| `DELETE` | `/products/{id}` | Owner or admin | Delete a product |

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/users` | Admin | List all users |
| `GET` | `/users/{id}` | Admin | Get one user |
| `POST` | `/users` | Admin | Create a user |
| `PUT` | `/users/{id}` | Admin | Replace a user's username, email and role |
| `DELETE` | `/users/{id}` | Admin | Delete a user **and all of their products** |

## Request and response examples

### Register

```http
POST /auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "password": "Password123!"
}
```

`201 Created`

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

| Field | Rules |
|---|---|
| `username` | Required, 3 to 20 characters, unique |
| `email` | Required, valid email, unique |
| `password` | Required, 8 to 72 characters |

### Log in

```http
POST /auth/login
Content-Type: application/json

{
  "identifier": "alice@example.com",
  "password": "Password123!"
}
```

`200 OK` with `{ "token": "..." }`. `identifier` accepts an email or a username.

### Create a product

```http
POST /products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Mechanical Keyboard",
  "description": "Brand new, still in the box.",
  "price": 79.99
}
```

`201 Created`

```json
{
  "id": "6af884f21a9c9f3b2da744b1",
  "name": "Mechanical Keyboard",
  "description": "Brand new, still in the box.",
  "price": 79.99,
  "userId": "6af8626ca4f5ba72f0700e07"
}
```

| Field | Rules |
|---|---|
| `name` | Required, 3 to 50 characters |
| `description` | Required, 3 to 255 characters |
| `price` | Required, between 0 and 1,000,000,000 |

The owner (`userId`) always comes from the token. A `userId` sent in the body is ignored, so nobody can create or move products into another user's account.

### Update a user (admin)

`PUT` replaces the whole resource, so send every field, including the unchanged ones:

```http
PUT /users/6af8626ca4f5ba72f0700e07
Authorization: Bearer <admin token>
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "role": "ADMIN"
}
```

`200 OK`

```json
{ "id": "6af8626ca4f5ba72f0700e07", "username": "alice", "email": "alice@example.com", "role": "ADMIN" }
```

`role` must be `USER` or `ADMIN`. Keeping your own email or username is allowed; choosing one that belongs to another user returns `409`.

### Delete

`DELETE /products/{id}` and `DELETE /users/{id}` return `200 OK`:

```json
{ "message": "Product deleted successfully" }
```

### With curl

```bash
TOKEN=$(curl -s --cacert letsplay.crt -X POST https://localhost:8443/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice@example.com","password":"Password123!"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl --cacert letsplay.crt -X POST https://localhost:8443/products \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Mechanical Keyboard","description":"Brand new","price":79.99}'
```

## Error handling

Every error is returned as JSON in the same format by a global exception handler. The API never returns stack traces or unhandled `5xx` errors.

```json
{
  "timestamp": "2026-11-18T13:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields.",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters"
  }
}
```

The `errors` object only appears on validation errors and lists each invalid field.

| Status | When |
|---|---|
| `400 Bad Request` | Invalid fields, malformed JSON, missing body, wrong value type |
| `401 Unauthorized` | Missing, invalid or expired token; wrong login credentials |
| `403 Forbidden` | Authenticated but not allowed (a user editing someone else's product, a user calling `/users`) |
| `404 Not Found` | Unknown product, user or endpoint |
| `405 Method Not Allowed` | Unsupported HTTP method on an existing endpoint |
| `409 Conflict` | Email or username already taken |
| `415 Unsupported Media Type` | Body not sent as `application/json` |

Login errors never say whether the account exists or the password was wrong, so they cannot be used to discover registered users.

## Security measures

| Measure | Implementation |
|---|---|
| Password hashing | BCrypt (salted automatically); passwords are never stored or returned in clear |
| Token authentication | Stateless JWT signed with a secret from `.env`, verified on every request by `JwtAuthenticationFilter` |
| Role-based access | URL rules in `SecurityConfig` plus ownership checks in `ProductService` |
| Input validation | Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Pattern`, ...) on every request DTO, with `@Valid` |
| NoSQL injection | Typed DTOs: an object such as `{"$ne": null}` sent instead of a string is rejected with `400`; repository methods never build queries from raw strings |
| Sensitive data | Responses use DTOs, so password hashes never appear in any response |
| HTTPS | TLS on port 8443 with a PKCS12 keystore |
| Secrets | `.env`, `keystore.p12` and `letsplay.crt` are excluded from git |
| Error responses | Stack traces disabled; one generic message for unexpected errors |

## Testing

Two Python scripts (standard library only, nothing to install) live in `scripts/`. Run them from the project root while the app is running.

**Full API test suite** (80 checks: auth, roles, ownership, validation, status codes, password exposure):

```bash
python3 scripts/test_api.py --base-url https://localhost:8443 --cacert letsplay.crt
```

Admin credentials are read from `.env`. The script prints `PASS`/`FAIL` for each check and a summary at the end.

**Seed test data** (users `seller1`, `seller2`, ... with products):

```bash
python3 scripts/seed_products.py --base-url https://localhost:8443 --cacert letsplay.crt
python3 scripts/seed_products.py --users 5 --products-per-user 10   # more data
python3 scripts/seed_products.py --clean                            # remove seed users and their products
```

Both scripts accept `--insecure` instead of `--cacert` to skip certificate verification, like `curl -k`.

## Project structure

```
src/main/java/letsplay/java
├── JavaApplication.java           Entry point
├── config/
│   ├── SecurityConfig.java        Filter chain, URL rules, BCrypt, AuthenticationManager
│   ├── JwtAuthenticationFilter.java   Reads the Bearer token and authenticates each request
│   ├── ApplicationConfig.java     UserDetailsService: loads users by email or username
│   ├── SecurityUser.java          Adapter between the User entity and Spring Security
│   └── DatabaseSeeder.java        Creates the admin account on startup
├── controller/                    AuthController, ProductController, UserController
├── service/                       AuthService, JwtService, ProductService, UserService
├── repository/                    UserRepository, ProductRepository (Spring Data MongoDB)
├── entity/                        User, Product (MongoDB documents)
├── dto/
│   ├── Request/                   Validated input records
│   └── Response/                  Output records (no sensitive fields)
└── exception/
    └── GlobalExceptionHandler.java    Consistent JSON errors
```

## Troubleshooting

| Problem | Fix |
|---|---|
| `Could not resolve placeholder 'JWT_SECRET'` (or another variable) | `.env` is missing or incomplete. Run the app from the project root, next to `.env` |
| `keystore.p12 (No such file or directory)` | Generate the keystore (step 3) in the project root |
| `curl: (60) SSL certificate problem` | Add `--cacert letsplay.crt`, or `-k` to skip verification |
| Browser shows "Your connection is not private" | Expected with a self-signed certificate; continue to the site |
| MongoDB authentication failed | `MONGO_USER`/`MONGO_PASSWORD` must match the values used when the container was **first** created. To reset: `docker compose down -v`, then `docker compose up -d` (this deletes the data) |
| Admin login returns `401` | The admin is only created if its email does not exist yet, so changing `.env` later has no effect. Delete the admin document in MongoDB and restart the app |
| Port 8443 already in use | Stop the other process, or change `server.port` in `application.yaml` |
