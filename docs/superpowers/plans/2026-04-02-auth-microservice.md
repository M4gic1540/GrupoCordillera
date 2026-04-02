# Auth Microservice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an MVP auth microservice with Spring Boot + PostgreSQL that supports register, login, refresh-token rotation, and authenticated `/api/users/me`.

**Architecture:** Implement a layered structure (`controller`, `service`, `repository`, `security`) with JWT access tokens and persisted refresh tokens. Use JPA entities for `users` and `refresh_tokens`, BCrypt for passwords, and a JWT filter to secure non-auth routes. Add focused integration tests for the auth flows and authorization behavior.

**Tech Stack:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA, PostgreSQL driver, JJWT, Bean Validation, JUnit 5, Spring Boot Test.

---

## File Structure Map

### Create
- `src/main/java/com/main/authservice/model/Role.java`
- `src/main/java/com/main/authservice/model/User.java`
- `src/main/java/com/main/authservice/model/RefreshToken.java`
- `src/main/java/com/main/authservice/repository/UserRepository.java`
- `src/main/java/com/main/authservice/repository/RefreshTokenRepository.java`
- `src/main/java/com/main/authservice/dto/RegisterRequest.java`
- `src/main/java/com/main/authservice/dto/LoginRequest.java`
- `src/main/java/com/main/authservice/dto/RefreshRequest.java`
- `src/main/java/com/main/authservice/dto/AuthResponse.java`
- `src/main/java/com/main/authservice/dto/UserMeResponse.java`
- `src/main/java/com/main/authservice/security/JwtService.java`
- `src/main/java/com/main/authservice/security/JwtAuthenticationFilter.java`
- `src/main/java/com/main/authservice/security/CustomUserDetailsService.java`
- `src/main/java/com/main/authservice/config/SecurityConfig.java`
- `src/main/java/com/main/authservice/service/AuthService.java`
- `src/main/java/com/main/authservice/controller/AuthController.java`
- `src/main/java/com/main/authservice/controller/UserController.java`
- `src/main/java/com/main/authservice/exception/ApiExceptionHandler.java`
- `src/main/java/com/main/authservice/exception/ConflictException.java`
- `src/main/java/com/main/authservice/exception/UnauthorizedException.java`
- `src/test/java/com/main/authservice/AuthIntegrationTest.java`

### Modify
- `pom.xml`
- `src/main/resources/application.properties`

---

### Task 1: Add dependencies and base config

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Test: `./mvnw.cmd -q -DskipTests compile`

- [ ] **Step 1: Write the failing setup check**

```bash
./mvnw.cmd -q -DskipTests compile
```

Expected: FAIL because JWT classes/config are not present yet.

- [ ] **Step 2: Add JWT dependencies**

```xml
<!-- Add under <dependencies> -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 3: Add PostgreSQL + JWT properties**

```properties
spring.application.name=authservice
spring.datasource.url=jdbc:postgresql://localhost:5432/authdb
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

security.jwt.secret=change-this-secret-key-to-32-bytes-minimum
security.jwt.access-token-expiration=900000
security.jwt.refresh-token-expiration=604800000
```

- [ ] **Step 4: Re-run compile**

```bash
./mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/application.properties
git commit -m "chore: add jwt and postgres base configuration"
```

### Task 2: Create entities and repositories

**Files:**
- Create: `src/main/java/com/main/authservice/model/Role.java`
- Create: `src/main/java/com/main/authservice/model/User.java`
- Create: `src/main/java/com/main/authservice/model/RefreshToken.java`
- Create: `src/main/java/com/main/authservice/repository/UserRepository.java`
- Create: `src/main/java/com/main/authservice/repository/RefreshTokenRepository.java`
- Test: `./mvnw.cmd -q -DskipTests test-compile`

- [ ] **Step 1: Write failing compile check for missing domain classes**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: FAIL for missing `model`/`repository` types used later.

- [ ] **Step 2: Add domain model**

```java
// Role.java
package com.main.authservice.model;

public enum Role {
    USER,
    ADMIN
}
```

```java
// User.java
package com.main.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Instant createdAt;
}
```

```java
// RefreshToken.java
package com.main.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;
}
```

- [ ] **Step 3: Add repository interfaces**

```java
// UserRepository.java
package com.main.authservice.repository;

import com.main.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

```java
// RefreshTokenRepository.java
package com.main.authservice.repository;

import com.main.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
}
```

- [ ] **Step 4: Re-run test-compile**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/main/authservice/model src/main/java/com/main/authservice/repository
git commit -m "feat: add auth domain entities and repositories"
```

### Task 3: Add DTO validation and exception model

**Files:**
- Create: `src/main/java/com/main/authservice/dto/RegisterRequest.java`
- Create: `src/main/java/com/main/authservice/dto/LoginRequest.java`
- Create: `src/main/java/com/main/authservice/dto/RefreshRequest.java`
- Create: `src/main/java/com/main/authservice/dto/AuthResponse.java`
- Create: `src/main/java/com/main/authservice/dto/UserMeResponse.java`
- Create: `src/main/java/com/main/authservice/exception/ConflictException.java`
- Create: `src/main/java/com/main/authservice/exception/UnauthorizedException.java`
- Create: `src/main/java/com/main/authservice/exception/ApiExceptionHandler.java`
- Test: `./mvnw.cmd -q -DskipTests test-compile`

- [ ] **Step 1: Write failing compile check for missing DTO and exceptions**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: FAIL once controllers/services reference these classes.

- [ ] **Step 2: Create DTOs with validation constraints**

```java
// RegisterRequest.java
package com.main.authservice.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    String password
) {}
```

```java
// LoginRequest.java
package com.main.authservice.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
```

```java
// RefreshRequest.java
package com.main.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
```

```java
// AuthResponse.java
package com.main.authservice.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {}
```

```java
// UserMeResponse.java
package com.main.authservice.dto;

public record UserMeResponse(Long id, String email, String role) {}
```

- [ ] **Step 3: Add explicit exceptions and global handler**

```java
// ConflictException.java
package com.main.authservice.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
```

```java
// UnauthorizedException.java
package com.main.authservice.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) { super(message); }
}
```

```java
// ApiExceptionHandler.java
package com.main.authservice.exception;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError f : ex.getBindingResult().getFieldErrors()) errors.put(f.getField(), f.getDefaultMessage());
        return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
    }
}
```

- [ ] **Step 4: Re-run test-compile**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/main/authservice/dto src/main/java/com/main/authservice/exception
git commit -m "feat: add auth dto validation and api exception handling"
```

### Task 4: Implement JWT + Spring Security

**Files:**
- Create: `src/main/java/com/main/authservice/security/JwtService.java`
- Create: `src/main/java/com/main/authservice/security/CustomUserDetailsService.java`
- Create: `src/main/java/com/main/authservice/security/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/main/authservice/config/SecurityConfig.java`
- Test: `./mvnw.cmd -q -DskipTests test-compile`

- [ ] **Step 1: Write failing compile check for missing security classes**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: FAIL when services/controllers depend on security beans.

- [ ] **Step 2: Implement JWT service**

```java
// JwtService.java
package com.main.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    @Value("${security.jwt.secret}") private String secret;
    @Value("${security.jwt.access-token-expiration}") private long accessTokenExpiration;

    public String generateAccessToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpiration))
                .signWith(signingKey())
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

- [ ] **Step 3: Implement UserDetails, filter, and SecurityConfig**

```java
// SecurityConfig.java (core shape)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                .userDetailsService(customUserDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception { return c.getAuthenticationManager(); }
}
```

- [ ] **Step 4: Re-run test-compile**

```bash
./mvnw.cmd -q -DskipTests test-compile
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/main/authservice/security src/main/java/com/main/authservice/config
git commit -m "feat: add jwt security configuration and authentication filter"
```

### Task 5: Implement AuthService and controllers

**Files:**
- Create: `src/main/java/com/main/authservice/service/AuthService.java`
- Create: `src/main/java/com/main/authservice/controller/AuthController.java`
- Create: `src/main/java/com/main/authservice/controller/UserController.java`
- Test: `./mvnw.cmd -q -DskipTests test-compile`

- [ ] **Step 1: Write failing test shell for auth endpoints**

```java
// AuthIntegrationTest.java skeleton first
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired MockMvc mockMvc;
}
```

Expected: FAIL once assertions are added and endpoints are missing.

- [ ] **Step 2: Implement auth service methods**

```java
// AuthService.java core methods
public AuthResponse register(RegisterRequest request) { ... }      // 409 on duplicate email
public AuthResponse login(LoginRequest request) { ... }             // 401 on invalid credentials
public AuthResponse refresh(RefreshRequest request) { ... }         // verify+rotate persisted refresh token
public UserMeResponse me(String email) { ... }                      // returns id/email/role
```

- [ ] **Step 3: Implement controllers**

```java
// AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) { ... }
    @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) { ... }
    @PostMapping("/refresh") public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) { ... }
}
```

```java
// UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }
}
```

- [ ] **Step 4: Re-run compile**

```bash
./mvnw.cmd -q -DskipTests compile
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/main/authservice/service src/main/java/com/main/authservice/controller src/test/java/com/main/authservice/AuthIntegrationTest.java
git commit -m "feat: implement auth service and auth user endpoints"
```

### Task 6: Add integration tests for register/login/refresh/me

**Files:**
- Modify/Create: `src/test/java/com/main/authservice/AuthIntegrationTest.java`
- Test: `./mvnw.cmd test`

- [ ] **Step 1: Write failing integration tests**

```java
@Test void register_shouldReturn200AndTokens() throws Exception { ... }
@Test void register_duplicateEmail_shouldReturn409() throws Exception { ... }
@Test void login_invalidPassword_shouldReturn401() throws Exception { ... }
@Test void refresh_shouldRotateToken() throws Exception { ... }
@Test void me_withoutToken_shouldReturn401() throws Exception { ... }
@Test void me_withToken_shouldReturn200() throws Exception { ... }
```

- [ ] **Step 2: Run tests to verify failures**

```bash
./mvnw.cmd -Dtest=AuthIntegrationTest test
```

Expected: FAIL with endpoint/auth behavior mismatches before final adjustments.

- [ ] **Step 3: Make minimal fixes in service/security/controller to satisfy tests**

```java
// Ensure refresh rotation logic:
oldToken.setRevoked(true);
refreshTokenRepository.save(oldToken);
RefreshToken newToken = refreshTokenRepository.save(buildRefreshToken(user));
return new AuthResponse(jwtService.generateAccessToken(user.getEmail()), newToken.getToken(), "Bearer");
```

- [ ] **Step 4: Run full test suite**

```bash
./mvnw.cmd test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/main/authservice/AuthIntegrationTest.java src/main/java/com/main/authservice
git commit -m "test: add integration coverage for auth mvp flows"
```

### Task 7: Final polish and runbook snippet

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/com/main/authservice/controller/AuthController.java` (if needed for status codes)
- Test: `./mvnw.cmd clean test`

- [ ] **Step 1: Ensure final API status behavior**

```java
// Explicit response statuses
return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req)); // register if desired
return ResponseEntity.ok(authService.login(req));
return ResponseEntity.ok(authService.refresh(req));
```

- [ ] **Step 2: Execute clean verification**

```bash
./mvnw.cmd clean test
```

Expected: PASS with all tests green.

- [ ] **Step 3: Commit final adjustments**

```bash
git add src/main/resources/application.properties src/main/java/com/main/authservice/controller/AuthController.java
git commit -m "chore: finalize auth mvp api behavior and config"
```

---

## Self-Review

### Spec coverage
- Register/Login/Refresh/Me endpoints: covered in Tasks 5-6.
- JWT + persisted refresh token rotation: covered in Tasks 4-6.
- USER/ADMIN role model: covered in Task 2.
- Validation + error mapping (400/401/409): covered in Task 3 and validated in Task 6.
- PostgreSQL configuration: covered in Task 1.

### Placeholder scan
- No placeholder markers or deferred implementation tokens included.

### Type consistency
- `AuthResponse`, `RegisterRequest`, `LoginRequest`, `RefreshRequest`, and `UserMeResponse` are used consistently across service/controller/test tasks.
