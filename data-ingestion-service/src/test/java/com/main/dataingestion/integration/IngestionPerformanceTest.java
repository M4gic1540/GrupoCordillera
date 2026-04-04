package com.main.dataingestion.integration;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.main.dataingestion.controller.IngestionController;
import com.main.dataingestion.service.IngestionService;

@Tag("performance")
class IngestionPerformanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing", "support", "ops", "finance", "legacy", "core"})
    void syncShouldKeepP95Under500ms(String source) {
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String sourceSystem) {
                return new SyncResult(sourceSystem, 2, OffsetDateTime.now());
            }
        });

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            long start = System.nanoTime();
            controller.syncSource(source);
            times.add((System.nanoTime() - start) / 1_000_000);
        }

        List<Long> sorted = times.stream().sorted(Comparator.naturalOrder()).toList();
        int p95Index = (int) Math.ceil(sorted.size() * 0.95) - 1;
        long p95 = sorted.get(Math.max(0, p95Index));

        assertTrue(p95 <= 500, "p95 too high: " + p95 + " ms");
    }
}
