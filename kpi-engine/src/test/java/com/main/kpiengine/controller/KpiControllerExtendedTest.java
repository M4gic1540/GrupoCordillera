package com.main.kpiengine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.main.kpiengine.dto.KpiSnapshotResponse;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import com.main.kpiengine.service.KpiEngineService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KpiControllerExtendedTest {

        @ParameterizedTest
        @ValueSource(strings = {"crm", "erp", "hr", "sales"})
        void recalculateShouldReturnExpectedSource(String source) {
                KpiEngineService service = new StubKpiEngineService(source);
                KpiController controller = new KpiController(service);

                RecalculateKpiRequest request = new RecalculateKpiRequest();
                request.setSourceSystem(source);
                request.setAffectedRecords(2);

                var response = controller.recalculate(request);

                assertEquals(200, response.getStatusCode().value());
                assertEquals(source, response.getBody().sourceSystem());
        }

        @ParameterizedTest
        @ValueSource(strings = {"crm", "erp", "hr", "sales"})
        void latestSnapshotsShouldReturnData(String source) {
                KpiEngineService service = new StubKpiEngineService(source);
                KpiController controller = new KpiController(service);

                var response = controller.getLatestSnapshots();

                assertEquals(200, response.getStatusCode().value());
                assertEquals(source, response.getBody().get(0).sourceSystem());
        }

        private static class StubKpiEngineService extends KpiEngineService {
                private final String source;

                StubKpiEngineService(String source) {
                        super(null, null);
                        this.source = source;
                }

                @Override
                public RecalculateKpiResponse recalculate(RecalculateKpiRequest request) {
                        return new RecalculateKpiResponse(source, 2, List.of());
                }

                @Override
                public List<KpiSnapshotResponse> getLatestSnapshots() {
                        return List.of(new KpiSnapshotResponse(
                                        "INGEST_THROUGHPUT",
                                        "Ingestion Throughput",
                                        source,
                                        2,
                                        BigDecimal.valueOf(2),
                                        OffsetDateTime.now()));
                }
        }
}
