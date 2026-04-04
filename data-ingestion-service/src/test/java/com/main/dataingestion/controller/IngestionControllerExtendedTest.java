package com.main.dataingestion.controller;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.main.dataingestion.service.IngestionService;

class IngestionControllerExtendedTest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp-1", "hr9", "source-a"})
    void shouldDelegateValidSourceSystems(String sourceSystem) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String source) {
                return new SyncResult(source, 1, OffsetDateTime.now());
            }
        });

        IngestionService.SyncResult result = controller.syncSource(sourceSystem).getBody();
        assertEquals(sourceSystem, result.sourceSystem());
    }

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales"})
    void shouldReturnProcessedRecordsFromService(String sourceSystem) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String source) {
                return new SyncResult(source, 7, OffsetDateTime.now());
            }
        });

        IngestionService.SyncResult result = controller.syncSource(sourceSystem).getBody();
        assertEquals(7, result.processedRecords());
    }
}
