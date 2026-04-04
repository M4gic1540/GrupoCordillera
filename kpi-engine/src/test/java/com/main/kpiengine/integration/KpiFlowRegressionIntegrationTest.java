package com.main.kpiengine.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.main.kpiengine.dto.KpiSnapshotResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KpiFlowRegressionIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "INGEST_THROUGHPUT",
            "DATA_QUALITY_INDEX",
            "KPI_3",
            "KPI_4",
            "KPI_5",
            "KPI_6",
            "KPI_7",
            "KPI_8",
            "KPI_9",
            "KPI_10",
            "KPI_11",
            "KPI_12",
            "KPI_13",
            "KPI_14",
            "KPI_15"
    })
    void snapshotRecordShouldKeepCode(String code) {
        KpiSnapshotResponse response = new KpiSnapshotResponse(
                code,
                "Name",
                "crm",
                1,
                BigDecimal.ONE,
                OffsetDateTime.now());

        assertEquals(code, response.kpiCode());
    }
}
