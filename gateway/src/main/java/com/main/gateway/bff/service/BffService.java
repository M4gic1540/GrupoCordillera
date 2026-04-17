
// Paquete service: lógica de agregación BFF
package com.main.gateway.bff.service;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.gateway.bff.config.BffProperties;
import com.main.gateway.bff.dto.BffDashboardResponse;

import reactor.core.publisher.Mono;


// Marca clase como servicio Spring
@Service
public class BffService {

    // WebClient para llamadas HTTP a otros microservicios
    private final WebClient webClient;
    // Propiedades de configuración BFF (URLs base)
    private final BffProperties bffProperties;

    // Constructor inyecta dependencias
    public BffService(WebClient webClient, BffProperties bffProperties) {
        this.webClient = webClient;
        this.bffProperties = bffProperties;
    }


        // Junta estado de ingesta, estado KPI y snapshots en una respuesta para dashboard
        public Mono<BffDashboardResponse> getDashboard() {
        Mono<Map<String, Object>> ingestionHealthMono = getMap(
            bffProperties.getIngestionBaseUrl(),
            "/api/ingestion/health",
            Map.of("status", "DOWN", "service", "data-ingestion-service")
        );

        Mono<Map<String, Object>> kpiHealthMono = getMap(
            bffProperties.getKpiBaseUrl(),
            "/api/kpi/health",
            Map.of("status", "DOWN", "service", "kpi-engine")
        );

        Mono<List<Map<String, Object>>> latestSnapshotsMono = getList(
            bffProperties.getKpiBaseUrl(),
            "/api/kpi/snapshots/latest"
        );

        // Espera respuestas y compone objeto final
        return Mono.zip(ingestionHealthMono, kpiHealthMono, latestSnapshotsMono)
            .map(tuple -> new BffDashboardResponse(tuple.getT1(), tuple.getT2(), tuple.getT3()));
        }


    // Ejecuta GET remoto, convierte a mapa, usa fallback si falla
    private Mono<Map<String, Object>> getMap(String baseUrl, String path, Map<String, Object> fallback) {
        return webClient.get()
                .uri(baseUrl + path)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::castMap)
                .onErrorReturn(fallback);
    }


    // Ejecuta GET remoto, convierte a lista de mapas, retorna lista vacía si falla
    private Mono<List<Map<String, Object>>> getList(String baseUrl, String path) {
        return webClient.get()
                .uri(baseUrl + path)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(this::castMap)
                .collectList()
                .onErrorReturn(Collections.emptyList());
    }

    // Normaliza mapa crudo deserializado hacia tipo esperado por el BFF
    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> raw) {
        return (Map<String, Object>) raw;
    }
}
