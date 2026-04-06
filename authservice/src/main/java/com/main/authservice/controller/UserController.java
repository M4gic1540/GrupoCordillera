package com.main.authservice.controller;

import com.main.authservice.dto.UserMeResponse;
import com.main.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para recursos de usuario autenticado.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for authenticated user data")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Retorna los datos del usuario actualmente autenticado.
     */
        @GetMapping("/me")
        @Operation(
            summary = "Get authenticated user",
            description = "Returns current authenticated user profile.",
            security = @SecurityRequirement(name = "bearerAuth")
        )
    public ResponseEntity<UserMeResponse> me(Authentication authentication) {
        logger.debug("User profile requested for principal={}", authentication.getName());
        UserMeResponse response = authService.me(authentication.getName());
        logger.debug("User profile response generated for userId={}", response.getId());
        return ResponseEntity.ok(response);
    }
}
