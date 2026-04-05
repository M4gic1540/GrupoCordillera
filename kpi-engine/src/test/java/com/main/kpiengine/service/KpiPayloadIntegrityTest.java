package com.main.kpiengine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.main.kpiengine.dto.KpiSnapshotResponse;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class KpiPayloadIntegrityTest {

    @Test
    void responsePayloadShouldKeepSourceAndRecords() {
        KpiSnapshotResponse snapshot = new KpiSnapshotResponse(
                "INGEST_THROUGHPUT", "Ingestion Throughput", "crm", 7,
                BigDecimal.valueOf(7), OffsetDateTime.now());

        RecalculateKpiResponse response = new RecalculateKpiResponse("crm", 7, List.of(snapshot));

        assertEquals("crm", response.sourceSystem());
        assertEquals(7, response.affectedRecords());
        assertEquals(1, response.snapshots().size());
    }
}
