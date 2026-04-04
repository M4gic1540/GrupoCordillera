package com.main.dataingestion.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.dataingestion.config.SourceSystemsProperties;
import com.main.dataingestion.connector.ConnectorFactory;
import com.main.dataingestion.connector.SourceConnector;
import com.main.dataingestion.repository.IngestionEventRepository;
import com.main.dataingestion.repository.SyncRunRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class IngestionServiceSecurityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"../etc/passwd", "${jndi:ldap://x}", "crm;drop", "<script>", "or 1=1", "x y", "x/y", "x\\y", "@admin", ""})
    void ingestShouldRejectUnknownOrMaliciousSourceKey(String source) {
        IngestionEventRepository eventRepository = Mockito.mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = Mockito.mock(SyncRunRepository.class);
        TestKpiNotificationService kpiService = new TestKpiNotificationService();

        ConnectorFactory connectorFactory = connectorFactoryRejectAll();

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiService);

        assertThrows(IllegalArgumentException.class, () -> service.ingest(source));
        verify(eventRepository, never()).saveAll(any());
        org.junit.jupiter.api.Assertions.assertEquals(0, kpiService.calls);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"payload\":\"<script>alert(1)</script>\"}", "{\"query\":\"' OR 1=1 --\"}", "{\"path\":\"../../etc/passwd\"}", "{\"cmd\":\"$(rm -rf /)\"}", "{\"token\":\"dotdot-path\"}", "{\"xss\":\"<img src=x onerror=1>\"}", "{\"key\":\"${env:HOME}\"}", "{\"field\":\"%0d%0a\"}", "{\"field\":\"\\u0000\\u0001\"}", "{\"field\":\"very-long-value\"}"})
    void ingestShouldProcessPotentiallyDangerousPayloadAsData(String jsonPayload) throws Exception {
        String source = "crm";
        SourceConnector connector = Mockito.mock(SourceConnector.class);
        IngestionEventRepository eventRepository = Mockito.mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = Mockito.mock(SyncRunRepository.class);
        TestKpiNotificationService kpiService = new TestKpiNotificationService();

        JsonNode payload = OBJECT_MAPPER.readTree(jsonPayload);

        ConnectorFactory connectorFactory = connectorFactoryStub(source, connector);
        when(connector.fetchBatch()).thenReturn(List.of(payload));
        when(eventRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiService);
        IngestionService.SyncResult result = service.ingest(source);

        org.junit.jupiter.api.Assertions.assertEquals(1, result.processedRecords());
        org.junit.jupiter.api.Assertions.assertEquals(1, kpiService.calls);
    }

    private ConnectorFactory connectorFactoryRejectAll() {
        SourceSystemsProperties props = new SourceSystemsProperties();
        props.setSystems(List.of());

        return new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults()) {
            @Override
            public SourceConnector getConnector(String sourceKey) {
                throw new IllegalArgumentException("Unknown source system");
            }
        };
    }

    private ConnectorFactory connectorFactoryStub(String expectedSource, SourceConnector connector) {
        SourceSystemsProperties props = new SourceSystemsProperties();
        props.setSystems(List.of());

        return new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults()) {
            @Override
            public SourceConnector getConnector(String sourceKey) {
                if (!expectedSource.equals(sourceKey)) {
                    throw new IllegalArgumentException("Unknown source system");
                }
                return connector;
            }
        };
    }

    private static class TestKpiNotificationService extends KpiNotificationService {
        private int calls = 0;

        TestKpiNotificationService() {
            super(WebClient.builder(), "http://localhost:1", "/api/kpi/recalculate");
        }

        @Override
        public void notifyRecalculation(String sourceSystem, int affectedRecords) {
            calls++;
        }
    }
}
