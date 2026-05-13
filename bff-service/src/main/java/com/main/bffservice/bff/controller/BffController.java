package com.main.bffservice.bff.controller;

/*
 * BffController - Controller REST.
 * Responsibilities: Punto de entrada HTTP y validacion de requests.
 * Patterns: MVC, BFF
 */


import com.main.bffservice.bff.dto.*;
import com.main.bffservice.bff.service.BffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff")
@Tag(name = "BFF", description = "Endpoints para la agregaciÃ³n de datos y proxy de servicios")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener Dashboard consolidado", description = "Recupera el estado de salud de los servicios y los Ãºltimos snapshots de KPIs.")
    public Mono<ResponseEntity<BffDashboardResponse>> getDashboard(@RequestParam Map<String, String> allParams) {
        return bffService.getDashboard(allParams).map(ResponseEntity::ok);
    }

    // --- Auth Endpoints ---

    @PostMapping("/auth/login")
    @Operation(summary = "Proxy Login")
    public Mono<AuthResponse> login(@RequestBody LoginRequest request) {
        return bffService.login(request);
    }

    @PostMapping("/auth/register")
    @Operation(summary = "Proxy Register")
    public Mono<AuthResponse> register(@RequestBody RegisterRequest request) {
        return bffService.register(request);
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Proxy Refresh Token")
    public Mono<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return bffService.refresh(request);
    }

    @GetMapping("/auth/me")
    @Operation(summary = "Proxy Get Current User")
    public Mono<UserResponse> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return bffService.me(authHeader);
    }

    @PutMapping("/users/me")
    @Operation(summary = "Proxy Update Current User")
    public Mono<UserResponse> updateCurrentUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody Map<String, Object> request) {
        return bffService.updateCurrentUser(authHeader, request);
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Proxy Update User Role")
    public Mono<UserResponse> updateUserRole(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        return bffService.updateUserRole(authHeader, userId, request);
    }

    // --- KPI Endpoints ---

    @GetMapping("/kpis/latest")
    @Operation(summary = "Proxy Get Latest KPIs")
    public Mono<List<KpiSnapshotResponse>> getLatestKpis() {
        return bffService.getLatestKpis();
    }

    @PostMapping("/kpis/recalculate")
    @Operation(summary = "Proxy Recalculate KPI")
    public Mono<RecalculateKpiResponse> recalculateKpi(@RequestBody RecalculateKpiRequest request) {
        return bffService.recalculateKpi(request);
    }

    // --- Ingestion Endpoints ---

    @PostMapping("/ingestion/sync/{sourceSystem}")
    @Operation(summary = "Proxy Sync Source")
    public Mono<Map<String, Object>> syncSource(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String sourceSystem) {
        return bffService.syncSource(authHeader, sourceSystem);
    }

    // --- System Endpoints ---

    @GetMapping("/system/health")
    @Operation(summary = "Get Health of all downstream services")
    public Mono<Map<String, HealthStatus>> getSystemHealth() {
        return bffService.getSystemHealth();
    }
}

