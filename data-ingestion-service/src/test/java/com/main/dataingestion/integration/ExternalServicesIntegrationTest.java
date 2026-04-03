package com.main.dataingestion.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import com.main.dataingestion.config.SourceSystemsProperties.SourceSystemConfig;
import com.main.dataingestion.connector.HttpSourceConnector;
import com.main.dataingestion.service.KpiNotificationService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ExternalServicesIntegrationTest {

    private static final int SOURCE_PORT = 18101;
    private static final int KPI_PORT = 18102;

    private static HttpServer sourceServer;
    private static HttpServer kpiServer;
    private static final AtomicBoolean kpiCalled = new AtomicBoolean(false);
    private static final AtomicReference<String> lastContentType = new AtomicReference<>(null);

    @BeforeAll
    static void setup() throws IOException {
        sourceServer = HttpServer.create(new InetSocketAddress(SOURCE_PORT), 0);
        sourceServer.createContext("/api/events", exchange -> {
            byte[] response = "[{\"id\":1001,\"type\":\"created\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        sourceServer.setExecutor(Executors.newSingleThreadExecutor());
        sourceServer.start();

        kpiServer = HttpServer.create(new InetSocketAddress(KPI_PORT), 0);
        kpiServer.createContext("/api/kpi/recalculate", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                kpiCalled.set(true);
                lastContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        kpiServer.setExecutor(Executors.newSingleThreadExecutor());
        kpiServer.start();
    }

    @AfterAll
    static void teardown() {
        if (sourceServer != null) {
            sourceServer.stop(0);
        }
        if (kpiServer != null) {
            kpiServer.stop(0);
        }
    }

    @Test
    void shouldFetchBatchFromSourceMicroservice() {
        SourceSystemConfig config = new SourceSystemConfig();
        config.setKey("crm");
        config.setBaseUrl("http://localhost:" + SOURCE_PORT);
        config.setTimeoutMs(2500);
        config.setEndpointPath("/api/events");
        config.setCircuitBreakerName("crmConnector");

        HttpSourceConnector connector = new HttpSourceConnector(
                config,
                WebClient.builder(),
                CircuitBreakerRegistry.ofDefaults()
        );

        List<JsonNode> result = connector.fetchBatch();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("id").asInt()).isEqualTo(1001);
    }

    @Test
    void shouldNotifyKpiServiceAfterIngestionAsGatewayFlow() {
        kpiCalled.set(false);
        lastContentType.set(null);

        KpiNotificationService kpiNotificationService = new KpiNotificationService(
                WebClient.builder(),
                "http://localhost:" + KPI_PORT,
                "/api/kpi/recalculate"
        );

        kpiNotificationService.notifyRecalculation("crm", 7);

        assertThat(kpiCalled.get()).isTrue();
        assertThat(lastContentType.get()).contains("application/json");
    }
}
