package com.main.authservice.controller;

import com.main.authservice.dto.AuthResponse;
import com.main.authservice.dto.BootstrapAdminRequest;
import com.main.authservice.dto.LoginRequest;
import com.main.authservice.dto.RefreshRequest;
import com.main.authservice.dto.RegisterRequest;
import com.main.authservice.security.JwtService;
import com.main.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Controlador REST para operaciones de autenticacion.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for register, login and token refresh")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Endpoint de registro de usuario.
     */
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Creates a new user and returns access and refresh tokens.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Register requested");
        AuthResponse response = authService.register(request);
        logger.info("Register completed for userId={}", response.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bootstrap de primer administrador cuando aun no existe ningun ADMIN.
     */
    @PostMapping("/bootstrap-admin")
    @Operation(summary = "Bootstrap first admin", description = "Creates or promotes first ADMIN user. Allowed only if no ADMIN exists.")
    public ResponseEntity<AuthResponse> bootstrapAdmin(@Valid @RequestBody BootstrapAdminRequest request) {
        logger.info("Bootstrap admin requested");
        AuthResponse response = authService.bootstrapAdmin(request);
        logger.info("Bootstrap admin completed for userId={}", response.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint de login con email y password.
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns access and refresh tokens.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login requested");
        AuthResponse response = authService.login(request);
        logger.info("Login completed for userId={}", response.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de renovacion de token de acceso mediante refresh token valido.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new token pair using a valid refresh token.")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        logger.info("Token refresh requested");
        AuthResponse response = authService.refresh(request);
        logger.info("Token refresh completed for userId={}", response.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Valida un access token recibido en el header Authorization.
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate access token", description = "Validates JWT token for gateway auth request checks.")
    public ResponseEntity<Void> validateAccessToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            logger.debug("JWT validation rejected: missing bearer header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorization.substring(7);
        try {
            if (jwtService.isTokenValid(token)) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception ex) {
            logger.debug("JWT validation rejected: invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
