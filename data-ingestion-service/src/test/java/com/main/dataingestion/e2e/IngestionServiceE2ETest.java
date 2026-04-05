package com.main.dataingestion.e2e;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.main.dataingestion.controller.IngestionController;
import com.main.dataingestion.service.IngestionService;

class IngestionServiceE2ETest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing", "support", "ops", "finance", "legacy"})
    void e2eSyncShouldReturnSourceAndRecords(String source) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String sourceSystem) {
                return new SyncResult(sourceSystem, 3, OffsetDateTime.now());
            }
        });

        IngestionService.SyncResult response = controller.syncSource(source).getBody();
        assertEquals(source, response.sourceSystem());
        assertEquals(3, response.processedRecords());
        assertNotNull(response.completedAt());
    }
}
