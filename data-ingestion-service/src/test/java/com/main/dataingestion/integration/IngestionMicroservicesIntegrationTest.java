package com.main.dataingestion.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.dataingestion.config.SourceSystemsProperties;
import com.main.dataingestion.config.SourceSystemsProperties.SourceSystemConfig;
import com.main.dataingestion.connector.ConnectorFactory;
import com.main.dataingestion.domain.IngestionEvent;
import com.main.dataingestion.domain.SyncRun;
import com.main.dataingestion.repository.IngestionEventRepository;
import com.main.dataingestion.repository.SyncRunRepository;
import com.main.dataingestion.service.IngestionService;
import com.main.dataingestion.service.KpiNotificationService;
import com.sun.net.httpserver.HttpServer;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class IngestionMicroservicesIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private HttpServer sourceServer;
    private HttpServer kpiServer;

    @AfterEach
    void tearDown() {
        if (sourceServer != null) {
            sourceServer.stop(0);
        }
        if (kpiServer != null) {
            kpiServer.stop(0);
        }
    }

    @Test
    void shouldIngestEventsAndNotifyKpiEngine() throws Exception {
        int sourcePort = findFreePort();
        int kpiPort = findFreePort();

        sourceServer = HttpServer.create(new InetSocketAddress(sourcePort), 0);
        sourceServer.createContext("/api/events", exchange -> {
            byte[] response = "[{\"id\":1},{\"id\":2}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        sourceServer.setExecutor(Executors.newSingleThreadExecutor());
        sourceServer.start();

        AtomicReference<String> kpiBody = new AtomicReference<>("");
        kpiServer = HttpServer.create(new InetSocketAddress(kpiPort), 0);
        kpiServer.createContext("/api/kpi/recalculate", exchange -> {
            byte[] body = exchange.getRequestBody().readAllBytes();
            kpiBody.set(new String(body, StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        kpiServer.setExecutor(Executors.newSingleThreadExecutor());
        kpiServer.start();

        SourceSystemsProperties props = new SourceSystemsProperties();
        SourceSystemConfig config = new SourceSystemConfig();
        config.setKey("crm");
        config.setBaseUrl("http://localhost:" + sourcePort);
        config.setEndpointPath("/api/events");
        config.setTimeoutMs(2500);
        config.setCircuitBreakerName("crmConnector");
        props.setSystems(List.of(config));

        ConnectorFactory connectorFactory = new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults());
        KpiNotificationService kpiNotificationService = new KpiNotificationService(
                WebClient.builder(),
                "http://localhost:" + kpiPort,
                "/api/kpi/recalculate"
        );

        IngestionEventRepository eventRepository = mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = mock(SyncRunRepository.class);
        when(eventRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiNotificationService);

        IngestionService.SyncResult result = null;
        // Avoid flaky failures in CI when local HTTP servers are not ready on the first call.
        for (int attempt = 0; attempt < 3; attempt++) {
            result = service.ingest("crm");
            if (result.processedRecords() == 2) {
                break;
            }
            Thread.sleep(120);
        }

        assertEquals("crm", result.sourceSystem());
        assertEquals(2, result.processedRecords());
        assertNotNull(result.completedAt());

        ArgumentCaptor<List<IngestionEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRepository).saveAll(eventsCaptor.capture());
        assertEquals(2, eventsCaptor.getValue().size());
        assertEquals("crm", eventsCaptor.getValue().get(0).getSourceSystem());

        verify(syncRunRepository).save(any(SyncRun.class));

        String payload = kpiBody.get();
        assertNotNull(payload);
        assertEquals("crm", OBJECT_MAPPER.readTree(payload).get("sourceSystem").asText());
        assertEquals(2, OBJECT_MAPPER.readTree(payload).get("affectedRecords").asInt());
    }

    @Test
    void shouldCompleteWithZeroRecordsWhenSourceIsUnavailable() {
        int kpiPort = findFreePort();

        AtomicReference<String> kpiBody = new AtomicReference<>("");
        try {
            kpiServer = HttpServer.create(new InetSocketAddress(kpiPort), 0);
            kpiServer.createContext("/api/kpi/recalculate", exchange -> {
                byte[] body = exchange.getRequestBody().readAllBytes();
                kpiBody.set(new String(body, StandardCharsets.UTF_8));
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            kpiServer.setExecutor(Executors.newSingleThreadExecutor());
            kpiServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SourceSystemsProperties props = new SourceSystemsProperties();
        SourceSystemConfig config = new SourceSystemConfig();
        config.setKey("crm");
        config.setBaseUrl("http://localhost:6553");
        config.setEndpointPath("/api/events");
        config.setTimeoutMs(250);
        config.setCircuitBreakerName("crmConnector");
        props.setSystems(List.of(config));

        ConnectorFactory connectorFactory = new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults());
        KpiNotificationService kpiNotificationService = new KpiNotificationService(
                WebClient.builder(),
                "http://localhost:" + kpiPort,
                "/api/kpi/recalculate"
        );

        IngestionEventRepository eventRepository = mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = mock(SyncRunRepository.class);
        when(eventRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiNotificationService);

        IngestionService.SyncResult result = service.ingest("crm");

        assertEquals(0, result.processedRecords());
        verify(eventRepository).saveAll(any());
        verify(syncRunRepository).save(any(SyncRun.class));
    }

    @Test
    void shouldFailWhenSourceSystemIsUnknown() {
        SourceSystemsProperties props = new SourceSystemsProperties();
        props.setSystems(List.of());

        ConnectorFactory connectorFactory = new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults());
        IngestionEventRepository eventRepository = mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = mock(SyncRunRepository.class);
        KpiNotificationService kpiNotificationService = new KpiNotificationService(
            WebClient.builder(),
            "http://localhost:1",
            "/api/kpi/recalculate"
        );

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiNotificationService);

        assertThrows(IllegalArgumentException.class, () -> service.ingest("crm"));
    }

    private int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Could not allocate free port", e);
        }
    }
}
