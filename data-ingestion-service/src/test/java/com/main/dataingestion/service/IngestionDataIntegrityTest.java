package com.main.dataingestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.dataingestion.config.SourceSystemsProperties;
import com.main.dataingestion.connector.ConnectorFactory;
import com.main.dataingestion.connector.SourceConnector;
import com.main.dataingestion.domain.IngestionEvent;
import com.main.dataingestion.domain.SyncRun;
import com.main.dataingestion.repository.IngestionEventRepository;
import com.main.dataingestion.repository.SyncRunRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

class IngestionDataIntegrityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void ingestShouldPersistOnlyValidAndUniquePayloads() throws Exception {
        String source = "crm";

        JsonNode valid1 = OBJECT_MAPPER.readTree("{\"id\":1,\"type\":\"created\",\"source\":\"crm\"}");
        JsonNode valid2 = OBJECT_MAPPER.readTree("{\"id\":2,\"type\":\"updated\",\"source\":\"crm\"}");
        JsonNode duplicateValid1 = OBJECT_MAPPER.readTree("{\"id\":1,\"type\":\"created\",\"source\":\"crm\"}");
        JsonNode missingType = OBJECT_MAPPER.readTree("{\"id\":3,\"source\":\"crm\"}");
        JsonNode wrongSource = OBJECT_MAPPER.readTree("{\"id\":4,\"type\":\"created\",\"source\":\"erp\"}");

        SourceConnector connector = mock(SourceConnector.class);
        when(connector.fetchBatch()).thenReturn(List.of(valid1, duplicateValid1, missingType, wrongSource, valid2));

        IngestionEventRepository eventRepository = mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = mock(SyncRunRepository.class);
        when(eventRepository.saveAll(org.mockito.ArgumentMatchers.<Iterable<IngestionEvent>>any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any(SyncRun.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectorFactory connectorFactory = connectorFactoryStub(source, connector);
        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, new NoopKpiNotificationService());

        IngestionService.SyncResult result = service.ingest(source);

        assertEquals(2, result.processedRecords());
    }

    private ConnectorFactory connectorFactoryStub(String expectedSource, SourceConnector connector) {
        SourceSystemsProperties props = new SourceSystemsProperties();
        props.setSystems(List.of());

        return new ConnectorFactory(props, WebClient.builder(), CircuitBreakerRegistry.ofDefaults()) {
            @Override
            public SourceConnector getConnector(String sourceKey) {
                if (!expectedSource.equals(sourceKey)) {
                    throw new IllegalArgumentException("Unknown source system: " + sourceKey);
                }
                return connector;
            }
        };
    }

    private static class NoopKpiNotificationService extends KpiNotificationService {

        NoopKpiNotificationService() {
            super(WebClient.builder(), "http://localhost:1", "/api/kpi/recalculate");
        }

        @Override
        public void notifyRecalculation(String sourceSystem, int affectedRecords) {
            // no-op for unit test
        }
    }
}
