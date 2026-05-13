package com.main.bffservice.bff.service;

/*
 * BffService - Service.
 * Responsibilities: Orquesta logica de negocio y reglas del dominio.
 * Patterns: Service Layer, BFF
 */


import com.main.bffservice.bff.config.BffProperties;
import com.main.bffservice.bff.dto.*;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BffService {

    private final WebClient webClient;
    private final BffProperties bffProperties;

    public BffService(WebClient.Builder webClientBuilder, BffProperties bffProperties) {
        this.webClient = webClientBuilder.build();
        this.bffProperties = bffProperties;
    }

    public Mono<BffDashboardResponse> getDashboard(Map<String, String> params) {
        Mono<HealthStatus> ingestionHealthMono = getHealth(
                bffProperties.getIngestionBaseUrl(),
                "/api/ingestion/health",
                "data-ingestion-service"
        );

        Mono<HealthStatus> kpiHealthMono = getHealth(
                bffProperties.getKpiBaseUrl(),
                "/api/kpi/health",
                "kpi-engine"
        );

        Mono<HealthStatus> authHealthMono = getHealth(
                bffProperties.getAuthBaseUrl(),
                "/api/auth/health",
                "auth-service"
        );

        Mono<List<KpiSnapshotResponse>> latestSnapshotsMono = webClient.get()
                .uri(bffProperties.getKpiBaseUrl() + "/api/kpi/snapshots/latest", uriBuilder -> {
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToFlux(KpiSnapshotResponse.class)
                .collectList()
                .timeout(Duration.ofSeconds(4))
                .onErrorReturn(Collections.emptyList());

        return Mono.zip(ingestionHealthMono, kpiHealthMono, authHealthMono, latestSnapshotsMono)
                .map(tuple -> new BffDashboardResponse(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
    }

    public Mono<Map<String, HealthStatus>> getSystemHealth() {
        return Mono.zip(
                getHealth(bffProperties.getIngestionBaseUrl(), "/api/ingestion/health", "data-ingestion-service"),
                getHealth(bffProperties.getKpiBaseUrl(), "/api/kpi/health", "kpi-engine"),
                getHealth(bffProperties.getAuthBaseUrl(), "/api/auth/health", "auth-service")
        ).map(tuple -> Map.of(
                "ingestion", tuple.getT1(),
                "kpi", tuple.getT2(),
                "auth", tuple.getT3()
        ));
    }

    // --- Auth Endpoints ---

    public Mono<AuthResponse> login(LoginRequest request) {
        return webClient.post()
                .uri(bffProperties.getAuthBaseUrl() + "/api/auth/login")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class);
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return webClient.post()
                .uri(bffProperties.getAuthBaseUrl() + "/api/auth/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class);
    }

    public Mono<AuthResponse> refresh(RefreshRequest request) {
        return webClient.post()
                .uri(bffProperties.getAuthBaseUrl() + "/api/auth/refresh")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AuthResponse.class);
    }

    public Mono<UserResponse> me(String authHeader) {
        return webClient.get()
                .uri(bffProperties.getAuthBaseUrl() + "/api/users/me")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    // --- KPI Endpoints ---

    public Mono<List<KpiSnapshotResponse>> getLatestKpis() {
        return webClient.get()
                .uri(bffProperties.getKpiBaseUrl() + "/api/kpi/snapshots/latest")
                .retrieve()
                .bodyToFlux(KpiSnapshotResponse.class)
                .collectList();
    }

    public Mono<RecalculateKpiResponse> recalculateKpi(RecalculateKpiRequest request) {
        return webClient.post()
                .uri(bffProperties.getKpiBaseUrl() + "/api/kpi/recalculate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecalculateKpiResponse.class);
    }

    // --- User Management Endpoints ---
    
    public Mono<UserResponse> updateCurrentUser(String authHeader, Map<String, Object> updateRequest) {
        return webClient.put()
                .uri(bffProperties.getAuthBaseUrl() + "/api/users/me/actualizarUser")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(updateRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    public Mono<UserResponse> updateUserRole(String authHeader, Long userId, Map<String, Object> roleRequest) {
        return webClient.patch()
                .uri(bffProperties.getAuthBaseUrl() + "/api/users/" + userId + "/role")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(roleRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    // --- Ingestion Endpoints ---

    public Mono<Map<String, Object>> syncSource(String authHeader, String sourceSystem) {
        return webClient.post()
                .uri(bffProperties.getIngestionBaseUrl() + "/api/ingestion/sync/" + sourceSystem)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> (Map<String, Object>) map);
    }

    private Mono<HealthStatus> getHealth(String baseUrl, String path, String serviceName) {
        return webClient.get()
                .uri(baseUrl + path)
                .retrieve()
                .bodyToMono(HealthStatus.class)
                .timeout(Duration.ofSeconds(4))
                .onErrorReturn(HealthStatus.down(serviceName));
    }
}

