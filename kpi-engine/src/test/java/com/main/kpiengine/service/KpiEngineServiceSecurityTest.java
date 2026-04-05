package com.main.kpiengine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.main.kpiengine.domain.KpiDefinition;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import com.main.kpiengine.repository.KpiDefinitionRepository;
import com.main.kpiengine.repository.KpiSnapshotRepository;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class KpiEngineServiceSecurityTest {

    @ParameterizedTest
    @ValueSource(strings = {"crm", "crm-prod", "erp1", "finance2", "ops-central", "legacy-v2", "hr-prod", "sales-api", "source9", "core"})
    void shouldPreserveSafeSourceSystemPatterns(String source) {
        KpiEngineService service = newService();

        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem(source);
        request.setAffectedRecords(1);

        RecalculateKpiResponse response = service.recalculate(request);

        assertEquals(source, response.sourceSystem());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 25, 50, 100, 300, 500, 1000, 1500, 5000})
    void shouldCapQualityIndexAtConfiguredUpperBound(int affectedRecords) {
        KpiEngineService service = newService();

        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("crm");
        request.setAffectedRecords(affectedRecords);

        RecalculateKpiResponse response = service.recalculate(request);

        assertEquals(2, response.snapshots().size());
        assertEquals(affectedRecords, response.affectedRecords());
    }

    private KpiEngineService newService() {
        KpiDefinitionRepository definitionRepository = Mockito.mock(KpiDefinitionRepository.class);
        KpiSnapshotRepository snapshotRepository = Mockito.mock(KpiSnapshotRepository.class);

        when(definitionRepository.findByCode(any())).thenReturn(Optional.of(mockDefinition()));
        when(snapshotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        return new KpiEngineService(definitionRepository, snapshotRepository);
    }

    private KpiDefinition mockDefinition() {
        KpiDefinition definition = new KpiDefinition();
        definition.setCode("DATA_QUALITY_INDEX");
        definition.setName("Data Quality Index");
        definition.setFrequency("EVENT_DRIVEN");
        return definition;
    }
}
