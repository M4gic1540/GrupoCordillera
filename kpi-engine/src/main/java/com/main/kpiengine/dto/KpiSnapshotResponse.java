package com.main.kpiengine.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de snapshot KPI expuesto por la API.
 *
 * @param kpiCode código funcional del indicador.
 * @param kpiName nombre legible del indicador.
 * @param sourceSystem sistema fuente asociado al cálculo.
 * @param affectedRecords volumen base considerado en el recálculo.
 * @param value valor numérico final del KPI.
 * @param computedAt fecha/hora exacta de cálculo del snapshot.
 */
public record KpiSnapshotResponse(
        String kpiCode,
        String kpiName,
        String sourceSystem,
        int affectedRecords,
        BigDecimal value,
        OffsetDateTime computedAt
) {
}
