package com.main.kpiengine.dto;

/*
 * RecalculateKpiResponse - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO
 */


import java.util.List;

/**
 * DTO de salida del endpoint de recÃ¡lculo KPI.
 *
 * @param sourceSystem sistema fuente que originÃ³ el cÃ¡lculo.
 * @param affectedRecords cantidad de registros usados como entrada.
 * @param snapshots snapshots KPI generados para la operaciÃ³n.
 */
public record RecalculateKpiResponse(
        String sourceSystem,
        int affectedRecords,
        List<KpiSnapshotResponse> snapshots
) {
}
