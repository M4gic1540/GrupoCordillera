package com.main.kpiengine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kpi")
@Tag(name = "Health", description = "Estado operativo del microservicio KPI")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Devuelve estado UP del servicio")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "kpi-engine");
    }
}
