package com.main.dataingestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion")
@Tag(name = "Health", description = "Estado operativo del microservicio")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Devuelve estado UP del servicio")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP", 
            "service", "data-ingestion-service",
            "details", Map.of("database", "UP", "timestamp", System.currentTimeMillis())
        );
    }
}
