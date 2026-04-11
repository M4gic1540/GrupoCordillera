package com.main.dataingestion.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.main.dataingestion.controller.IngestionController;
import com.main.dataingestion.service.IngestionService;

class IngestionConcurrencyTest {

    @Test
    void syncEndpointShouldHandleConcurrentRequestsWithoutDegradation() throws Exception {
        int concurrentRequests = 30;
        IngestionController controller = new IngestionController(new IngestionService(null, null, null, null) {
            @Override
            public SyncResult ingest(String sourceSystem) {
                return new SyncResult(sourceSystem, 2, OffsetDateTime.now());
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<ResponseEntity<IngestionService.SyncResult>>> tasks = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            int index = i;
            tasks.add(() -> controller.syncSource(index % 2 == 0 ? "crm" : "erp"));
        }

        long startNanos = System.nanoTime();
        List<Future<ResponseEntity<IngestionService.SyncResult>>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        for (Future<ResponseEntity<IngestionService.SyncResult>> future : futures) {
            ResponseEntity<IngestionService.SyncResult> response = getFutureValue(future);
            assertEquals(200, response.getStatusCode().value());
            assertEquals(2, response.getBody().processedRecords());
        }

        // Total batch under 2s demonstrates acceptable behavior under concurrent calls.
        assertTrue(elapsedMillis < 2000, "Concurrent execution exceeded threshold: " + elapsedMillis + "ms");
    }

    private ResponseEntity<IngestionService.SyncResult> getFutureValue(
            Future<ResponseEntity<IngestionService.SyncResult>> future) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException ex) {
            throw new AssertionError("Concurrent task failed", ex.getCause());
        }
    }
}
