package com.main.dataingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.main.dataingestion.connector.ConnectorFactory;
import com.main.dataingestion.connector.SourceConnector;
import com.main.dataingestion.domain.IngestionEvent;
import com.main.dataingestion.domain.SyncRun;
import com.main.dataingestion.repository.IngestionEventRepository;
import com.main.dataingestion.repository.SyncRunRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);

    private final ConnectorFactory connectorFactory;
    private final IngestionEventRepository eventRepository;
    private final SyncRunRepository syncRunRepository;
    private final KpiNotificationService kpiNotificationService;

    public IngestionService(ConnectorFactory connectorFactory,
                            IngestionEventRepository eventRepository,
                            SyncRunRepository syncRunRepository,
                            KpiNotificationService kpiNotificationService) {
        this.connectorFactory = connectorFactory;
        this.eventRepository = eventRepository;
        this.syncRunRepository = syncRunRepository;
        this.kpiNotificationService = kpiNotificationService;
    }

    @Transactional
    public SyncResult ingest(String sourceSystem) {
        OffsetDateTime start = OffsetDateTime.now();
        SourceConnector connector = connectorFactory.getConnector(sourceSystem);

        List<JsonNode> rawBatch = connector.fetchBatch();
        if (rawBatch == null) {
            rawBatch = List.of();
        }
        List<IngestionEvent> events = new ArrayList<>();
        Set<String> dedupKeys = new HashSet<>();
        int rejectedCount = 0;

        for (JsonNode payload : rawBatch) {
            if (!isPayloadValid(payload, sourceSystem)) {
                rejectedCount++;
                continue;
            }

            String dedupKey = payload.toString();
            if (!dedupKeys.add(dedupKey)) {
                logger.warn("Rejected duplicated payload for source={}", sourceSystem);
                rejectedCount++;
                continue;
            }

            IngestionEvent event = new IngestionEvent();
            event.setSourceSystem(sourceSystem);
            event.setIngestedAt(OffsetDateTime.now());
            event.setPayload(payload);
            events.add(event);
        }

        eventRepository.saveAll(events);

        SyncRun run = new SyncRun();
        run.setSourceSystem(sourceSystem);
        run.setRecordsProcessed(events.size());
        run.setStartedAt(start);
        run.setCompletedAt(OffsetDateTime.now());
        run.setStatus("COMPLETED");
        syncRunRepository.save(run);

        kpiNotificationService.notifyRecalculation(sourceSystem, events.size());
        logger.info("Ingestion completed for source={}, records={}, rejected={}", sourceSystem, events.size(), rejectedCount);

        return new SyncResult(sourceSystem, events.size(), run.getCompletedAt());
    }

    public record SyncResult(String sourceSystem, int processedRecords, OffsetDateTime completedAt) {
    }

    private boolean isPayloadValid(JsonNode payload, String sourceSystem) {
        if (payload == null || !payload.isObject()) {
            logger.warn("Rejected payload for source={} due to invalid structure", sourceSystem);
            return false;
        }

        JsonNode sourceNode = payload.get("source");
        if (sourceNode != null && sourceNode.isTextual() && !sourceSystem.equalsIgnoreCase(sourceNode.asText())) {
            logger.warn("Rejected payload for source={} due to source mismatch", sourceSystem);
            return false;
        }

        JsonNode typeNode = payload.get("type");
        // When source metadata is declared, event type becomes mandatory for consistency.
        if (sourceNode != null && (typeNode == null || !typeNode.isTextual() || typeNode.asText().isBlank())) {
            logger.warn("Rejected payload for source={} due to missing/invalid type", sourceSystem);
            return false;
        }

        return true;
    }
}
