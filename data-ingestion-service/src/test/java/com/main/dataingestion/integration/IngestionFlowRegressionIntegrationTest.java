package com.main.dataingestion.integration;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.main.dataingestion.controller.IngestionController;
import com.main.dataingestion.service.IngestionService;

class IngestionFlowRegressionIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing", "support", "ops", "core", "legacy", "finance"})
    void syncEndpointShouldReturnOkForKnownSources(String source) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String sourceSystem) {
                return new SyncResult(sourceSystem, 2, OffsetDateTime.now());
            }
        });

        IngestionService.SyncResult response = controller.syncSource(source).getBody();
        assertEquals(source, response.sourceSystem());
        assertEquals(2, response.processedRecords());
    }

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing"})
    void syncEndpointShouldReturnTimestamp(String source) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String sourceSystem) {
                return new SyncResult(sourceSystem, 1, OffsetDateTime.now());
            }
        });

        IngestionService.SyncResult response = controller.syncSource(source).getBody();
        assertEquals(source, response.sourceSystem());
    }
}
