package com.main.kpiengine.dto;

/*
 * KpiSnapshotResponse - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO
 */


import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de snapshot KPI expuesto por la API.
 *
 * @param kpiCode cÃ³digo funcional del indicador.
 * @param kpiName nombre legible del indicador.
 * @param sourceSystem sistema fuente asociado al cÃ¡lculo.
 * @param affectedRecords volumen base considerado en el recÃ¡lculo.
 * @param value valor numÃ©rico final del KPI.
 * @param computedAt fecha/hora exacta de cÃ¡lculo del snapshot.
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
