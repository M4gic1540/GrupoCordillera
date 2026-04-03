package com.main.kpiengine.service;

import com.main.kpiengine.domain.KpiDefinition;
import com.main.kpiengine.domain.KpiSnapshot;
import com.main.kpiengine.dto.KpiSnapshotResponse;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import com.main.kpiengine.repository.KpiDefinitionRepository;
import com.main.kpiengine.repository.KpiSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiEngineService {

    private final KpiDefinitionRepository definitionRepository;
    private final KpiSnapshotRepository snapshotRepository;

    public KpiEngineService(KpiDefinitionRepository definitionRepository,
                            KpiSnapshotRepository snapshotRepository) {
        this.definitionRepository = definitionRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public RecalculateKpiResponse recalculate(RecalculateKpiRequest request) {
        int affectedRecords = request.getAffectedRecords();

        KpiDefinition throughputKpi = ensureDefinition("INGEST_THROUGHPUT", "Ingestion Throughput", "EVENT_DRIVEN");
        KpiDefinition qualityKpi = ensureDefinition("DATA_QUALITY_INDEX", "Data Quality Index", "EVENT_DRIVEN");

        OffsetDateTime now = OffsetDateTime.now();
        KpiSnapshot throughputSnapshot = buildSnapshot(throughputKpi, request, now, BigDecimal.valueOf(affectedRecords));
        KpiSnapshot qualitySnapshot = buildSnapshot(qualityKpi, request, now, computeQualityIndex(affectedRecords));

        snapshotRepository.saveAll(List.of(throughputSnapshot, qualitySnapshot));

        return new RecalculateKpiResponse(
                request.getSourceSystem(),
                affectedRecords,
                List.of(toResponse(throughputSnapshot), toResponse(qualitySnapshot))
        );
    }

    @Transactional(readOnly = true)
    public List<KpiSnapshotResponse> getLatestSnapshots() {
        return snapshotRepository.findTop20ByOrderByComputedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private KpiDefinition ensureDefinition(String code, String name, String frequency) {
        return definitionRepository.findByCode(code)
                .orElseGet(() -> {
                    KpiDefinition definition = new KpiDefinition();
                    definition.setCode(code);
                    definition.setName(name);
                    definition.setFrequency(frequency);
                    return definitionRepository.save(definition);
                });
    }

    private KpiSnapshot buildSnapshot(KpiDefinition definition,
                                      RecalculateKpiRequest request,
                                      OffsetDateTime computedAt,
                                      BigDecimal value) {
        KpiSnapshot snapshot = new KpiSnapshot();
        snapshot.setDefinition(definition);
        snapshot.setSourceSystem(request.getSourceSystem());
        snapshot.setAffectedRecords(request.getAffectedRecords());
        snapshot.setComputedAt(computedAt);
        snapshot.setValue(value);
        return snapshot;
    }

    private BigDecimal computeQualityIndex(int affectedRecords) {
        if (affectedRecords <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal capped = BigDecimal.valueOf(Math.min(affectedRecords, 1000));
        return capped.multiply(BigDecimal.valueOf(0.1)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private KpiSnapshotResponse toResponse(KpiSnapshot snapshot) {
        return new KpiSnapshotResponse(
                snapshot.getDefinition().getCode(),
                snapshot.getDefinition().getName(),
                snapshot.getSourceSystem(),
                snapshot.getAffectedRecords(),
                snapshot.getValue(),
                snapshot.getComputedAt()
        );
    }
}
