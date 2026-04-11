package com.main.dataingestion.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
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

class IngestionServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing", "support", "ops", "finance", "legacy", "core"})
    void ingestShouldReturnTwoProcessedRecords(String source) throws Exception {
        SourceConnector connector = Mockito.mock(SourceConnector.class);
        IngestionEventRepository eventRepository = Mockito.mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = Mockito.mock(SyncRunRepository.class);
        TestKpiNotificationService kpiService = new TestKpiNotificationService();

        JsonNode p1 = OBJECT_MAPPER.readTree("{\"id\":1,\"type\":\"created\",\"source\":\"" + source + "\"}");
        JsonNode p2 = OBJECT_MAPPER.readTree("{\"id\":2,\"type\":\"updated\",\"source\":\"" + source + "\"}");

        when(connector.fetchBatch()).thenReturn(List.of(p1, p2));
        when(eventRepository.saveAll(org.mockito.ArgumentMatchers.<Iterable<IngestionEvent>>any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any(SyncRun.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectorFactory connectorFactory = connectorFactoryStub(source, connector);

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiService);

        IngestionService.SyncResult result = service.ingest(source);

        assertEquals(source, result.sourceSystem());
        assertEquals(2, result.processedRecords());
        assertEquals(1, kpiService.calls);
    }

    @ParameterizedTest
    @ValueSource(strings = {"source1", "source2", "source3", "source4", "source5", "source6", "source7", "source8", "source9", "source10"})
    void ingestShouldReturnZeroWhenConnectorReturnsEmptyBatch(String source) {
        SourceConnector connector = Mockito.mock(SourceConnector.class);
        IngestionEventRepository eventRepository = Mockito.mock(IngestionEventRepository.class);
        SyncRunRepository syncRunRepository = Mockito.mock(SyncRunRepository.class);
        TestKpiNotificationService kpiService = new TestKpiNotificationService();

        when(connector.fetchBatch()).thenReturn(List.of());
        when(eventRepository.saveAll(org.mockito.ArgumentMatchers.<Iterable<IngestionEvent>>any())).thenAnswer(inv -> inv.getArgument(0));
        when(syncRunRepository.save(any(SyncRun.class))).thenAnswer(inv -> inv.getArgument(0));

        ConnectorFactory connectorFactory = connectorFactoryStub(source, connector);

        IngestionService service = new IngestionService(connectorFactory, eventRepository, syncRunRepository, kpiService);

        IngestionService.SyncResult result = service.ingest(source);

        assertEquals(0, result.processedRecords());
        assertEquals(1, kpiService.calls);
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
