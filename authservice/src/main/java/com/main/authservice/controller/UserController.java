package com.main.authservice.controller;

import com.main.authservice.dto.UpdateUserRequest;
import com.main.authservice.dto.UpdateUserRoleRequest;
import com.main.authservice.dto.UserMeResponse;
import com.main.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        @PreAuthorize("hasAnyRole('USER','ADMIN')")
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

    /**
     * Actualiza los datos del usuario autenticado.
     */
    @PutMapping("/me/actualizarUser")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(
            summary = "Update authenticated user",
            description = "Updates email and password of current authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserMeResponse> actualizarUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        logger.info("Update request received for principal={}", authentication.getName());
        UserMeResponse response = authService.actualizarUser(authentication.getName(), request);
        logger.info("Update completed for userId={}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Permite a un administrador actualizar el rol de un usuario.
     */
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user role (admin)",
            description = "Updates target user role. Requires ADMIN role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserMeResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        logger.info("Role update requested for userId={} newRole={}", userId, request.getRole());
        UserMeResponse response = authService.updateUserRole(userId, request.getRole());
        logger.info("Role update completed for userId={} role={}", response.getId(), response.getRole());
        return ResponseEntity.ok(response);
    }
}
