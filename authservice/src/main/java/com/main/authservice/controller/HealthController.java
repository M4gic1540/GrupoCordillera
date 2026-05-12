package com.main.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Health", description = "Estado operativo del microservicio de autenticación")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Devuelve estado UP del servicio")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP", 
            "service", "auth-service",
            "details", Map.of("database", "UP", "timestamp", System.currentTimeMillis())
        );
    }
}
