package com.main.kpiengine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.main.kpiengine.domain.KpiDefinition;
import com.main.kpiengine.domain.KpiSnapshot;
import com.main.kpiengine.dto.KpiSnapshotResponse;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import com.main.kpiengine.repository.KpiDefinitionRepository;
import com.main.kpiengine.repository.KpiSnapshotRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class KpiEngineServiceTest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "erp", "hr", "sales", "billing", "support", "ops", "finance", "legacy", "core"})
    void recalculateShouldReturnTwoSnapshotsForKnownSource(String source) {
        KpiDefinitionRepository definitionRepository = Mockito.mock(KpiDefinitionRepository.class);
        KpiSnapshotRepository snapshotRepository = Mockito.mock(KpiSnapshotRepository.class);

        when(definitionRepository.findByCode(any())).thenReturn(Optional.of(mockDefinition()));
        when(snapshotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        KpiEngineService service = new KpiEngineService(definitionRepository, snapshotRepository);

        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem(source);
        request.setAffectedRecords(2);

        RecalculateKpiResponse response = service.recalculate(request);

        assertEquals(source, response.sourceSystem());
        assertEquals(2, response.affectedRecords());
        assertEquals(2, response.snapshots().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 20, 50, 100, 200, 500, 1000})
    void latestSnapshotsShouldMapRepositoryData(int affectedRecords) {
        KpiDefinitionRepository definitionRepository = Mockito.mock(KpiDefinitionRepository.class);
        KpiSnapshotRepository snapshotRepository = Mockito.mock(KpiSnapshotRepository.class);

        when(snapshotRepository.findTop20ByOrderByComputedAtDesc())
                .thenReturn(List.of(mockSnapshot("crm", affectedRecords), mockSnapshot("erp", affectedRecords)));

        KpiEngineService service = new KpiEngineService(definitionRepository, snapshotRepository);

        List<KpiSnapshotResponse> snapshots = service.getLatestSnapshots();

        assertEquals(2, snapshots.size());
        assertEquals(affectedRecords, snapshots.get(0).affectedRecords());
        assertNotNull(snapshots.get(0).computedAt());
    }

    private KpiDefinition mockDefinition() {
        KpiDefinition definition = new KpiDefinition();
        definition.setCode("INGEST_THROUGHPUT");
        definition.setName("Ingestion Throughput");
        definition.setFrequency("EVENT_DRIVEN");
        return definition;
    }

    private KpiSnapshot mockSnapshot(String source, int affectedRecords) {
        KpiSnapshot snapshot = new KpiSnapshot();
        snapshot.setDefinition(mockDefinition());
        snapshot.setSourceSystem(source);
        snapshot.setAffectedRecords(affectedRecords);
        snapshot.setValue(java.math.BigDecimal.valueOf(affectedRecords));
        snapshot.setComputedAt(java.time.OffsetDateTime.now());
        return snapshot;
    }
}
