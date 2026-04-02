# Auth Microservice Design (MVP)

## Problem and Goal
Create an authentication microservice with Spring Boot and PostgreSQL that supports secure user access for other services.  
This MVP must include register, login, token refresh, and current-user retrieval (`/me`) with role support (`USER`, `ADMIN`).

## Scope
### In scope
- User registration with password policy validation
- Login with email/password
- JWT access token issuance
- Refresh token issuance and rotation (persisted in PostgreSQL)
- Authenticated endpoint to retrieve current user profile
- Basic role model (`USER`, `ADMIN`)
- Standard API error handling for validation/auth/conflict errors

### Out of scope
- Email verification
- Password reset/recovery
- External OAuth providers
- Account lockout/rate limiting

## Recommended Approach
Use **stateless JWT access tokens** plus **stateful refresh tokens** stored in PostgreSQL.  
This balances performance (fast access-token checks) and control (refresh-token revocation/rotation).

## Architecture
Layered Spring Boot architecture:
- `controller`: HTTP contracts and request validation
- `service`: auth business logic, token issuance, rotation, and credential checks
- `repository`: JPA persistence for users and refresh tokens
- `security`: JWT filter and Spring Security configuration

Security model:
- Public routes: `/api/auth/**`
- Protected routes: `/api/users/me`
- Password hashing via BCrypt
- Access token read from `Authorization: Bearer <token>`

## Components
### Domain
- `User`
  - `id`, `email` (unique), `passwordHash`, `role`, timestamps
- `RefreshToken`
  - `id`, `token` (unique), `expiresAt`, `revoked`, `user`, timestamps

### Repositories
- `UserRepository`
  - `findByEmail(...)`, `existsByEmail(...)`
- `RefreshTokenRepository`
  - token lookup and revocation queries

### Services
- `AuthService`
  - register, login, refresh flow, token rotation
- `JwtService`
  - generate/parse/validate access tokens
- `CustomUserDetailsService`
  - load users for Spring Security

### API Endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/users/me`

## Data Flow
1. **Register**
   - Validate payload and password format
   - Reject duplicated email (`409`)
   - Hash password, store user with default `USER` role
2. **Login**
   - Validate credentials
   - On success, issue access token + refresh token
   - Persist refresh token with expiration
3. **Refresh**
   - Validate refresh token exists, not revoked, not expired
   - Revoke old refresh token
   - Issue new access token + new refresh token
   - Persist new refresh token
4. **Me**
   - JWT filter authenticates request
   - Return authenticated user profile

## Validation and Error Handling
- `400 Bad Request`: invalid DTO fields / password policy violation
- `401 Unauthorized`: invalid credentials or invalid/expired token
- `409 Conflict`: email already registered

Password policy:
- At least 8 chars
- At least one uppercase letter
- At least one lowercase letter
- At least one number

## PostgreSQL and Configuration
- Use PostgreSQL datasource in `application.properties`
- Use JPA/Hibernate for schema mapping (`users`, `refresh_tokens`)
- JWT config keys:
  - `security.jwt.secret`
  - `security.jwt.access-token-expiration`
  - `security.jwt.refresh-token-expiration`

## Testing Strategy
Integration tests for:
- Register success and duplicate email
- Login success and invalid credentials
- Refresh success and revoked/expired token
- `/api/users/me` with valid token and without token

## Implementation Notes
- Keep endpoints and DTOs minimal for MVP
- Keep refresh token rotation mandatory to reduce replay risk
- Ensure all timestamps are timezone-safe (`Instant`)
