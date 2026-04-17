package com.main.kpiengine.dto;

import java.util.List;

/**
 * DTO de salida del endpoint de recálculo KPI.
 *
 * @param sourceSystem sistema fuente que originó el cálculo.
 * @param affectedRecords cantidad de registros usados como entrada.
 * @param snapshots snapshots KPI generados para la operación.
 */
public record RecalculateKpiResponse(
        String sourceSystem,
        int affectedRecords,
        List<KpiSnapshotResponse> snapshots
) {
}
