package com.main.kpiengine.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.main.kpiengine.domain.KpiDefinition;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.repository.KpiDefinitionRepository;
import com.main.kpiengine.repository.KpiSnapshotRepository;
import com.main.kpiengine.service.KpiEngineService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class KpiEngineMicroservicesIntegrationTest {

    private KpiEngineService service;

    @BeforeEach
    void setUp() {
        KpiDefinitionRepository definitionRepository = Mockito.mock(KpiDefinitionRepository.class);
        KpiSnapshotRepository snapshotRepository = Mockito.mock(KpiSnapshotRepository.class);

        KpiDefinition definition = new KpiDefinition();
        definition.setCode("INGEST_THROUGHPUT");
        definition.setName("Ingestion Throughput");
        definition.setFrequency("EVENT_DRIVEN");

        when(definitionRepository.findByCode(any())).thenReturn(Optional.of(definition));
        when(snapshotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service = new KpiEngineService(definitionRepository, snapshotRepository);
    }

    @Test
    void shouldRecalculateForCrm() {
        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("crm");
        request.setAffectedRecords(2);

        var response = service.recalculate(request);

        assertEquals("crm", response.sourceSystem());
    }

    @Test
    void shouldRecalculateForErp() {
        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("erp");
        request.setAffectedRecords(3);

        var response = service.recalculate(request);

        assertEquals(3, response.affectedRecords());
    }

    @Test
    void shouldReturnTwoSnapshots() {
        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("ops");
        request.setAffectedRecords(1);

        var response = service.recalculate(request);

        assertNotNull(response.snapshots());
        assertEquals(2, response.snapshots().size());
    }
}
