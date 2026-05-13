package com.main.kpiengine.service;

/*
 * KpiEngineService - Service.
 * Responsibilities: Orquesta logica de negocio y reglas del dominio.
 * Patterns: Service Layer
 */


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
/**
 * Servicio de negocio para cÃ¡lculo y consulta de indicadores (KPI).
 *
 * <p>Orquesta repositorios de definiciÃ³n/snapshot para:
 * 1) asegurar catÃ¡logo mÃ­nimo de KPIs,
 * 2) calcular snapshots por evento de ingestiÃ³n,
 * 3) exponer resultados listos para API.</p>
 */
public class KpiEngineService {

    private final KpiDefinitionRepository definitionRepository;
    private final KpiSnapshotRepository snapshotRepository;

    public KpiEngineService(KpiDefinitionRepository definitionRepository,
                            KpiSnapshotRepository snapshotRepository) {
        this.definitionRepository = definitionRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Recalcula snapshots de KPI a partir del origen afectado y cantidad de registros.
     *
     * @param request payload con sistema origen y volumen impactado.
     * @return respuesta con snapshots recalculados para throughput y calidad.
     */
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

    /**
     * Consulta los snapshots mÃ¡s recientes para visualizaciÃ³n en dashboard/API.
     *
     * @return hasta 20 snapshots ordenados del mÃ¡s reciente al mÃ¡s antiguo.
     */
    @Transactional(readOnly = true)
    public List<KpiSnapshotResponse> getLatestSnapshots() {
        return snapshotRepository.findTop20ByOrderByComputedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Garantiza existencia de una definiciÃ³n KPI; si no existe, la crea.
     *
     * @param code identificador Ãºnico funcional del KPI.
     * @param name nombre legible para API/dashboard.
     * @param frequency frecuencia semÃ¡ntica de actualizaciÃ³n.
     * @return definiciÃ³n existente o reciÃ©n persistida.
     */
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

    /**
     * Construye una entidad snapshot con metadatos de recÃ¡lculo.
     *
     * @param definition definiciÃ³n KPI asociada.
     * @param request payload origen del recÃ¡lculo.
     * @param computedAt instante de cÃ¡lculo.
     * @param value valor numÃ©rico calculado del indicador.
     * @return snapshot listo para persistencia.
     */
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

    /**
     * Calcula un Ã­ndice de calidad simplificado a partir de volumen procesado.
     *
     * <p>La funciÃ³n limita mÃ¡ximos para evitar valores desproporcionados y
     * normaliza con escala fija para consistencia en reportes.</p>
     *
     * @param affectedRecords cantidad de registros que detonaron recÃ¡lculo.
     * @return Ã­ndice de calidad redondeado a 2 decimales.
     */
    private BigDecimal computeQualityIndex(int affectedRecords) {
        if (affectedRecords <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal capped = BigDecimal.valueOf(Math.min(affectedRecords, 1000));
        return capped.multiply(BigDecimal.valueOf(0.1)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Convierte entidad de dominio a DTO de salida.
     *
     * @param snapshot entidad persistida de KPI.
     * @return DTO serializable para respuesta API.
     */
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
