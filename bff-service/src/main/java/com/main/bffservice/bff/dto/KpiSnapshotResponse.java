package com.main.bffservice.bff.dto;

/*
 * KpiSnapshotResponse - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO, BFF
 */


import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record KpiSnapshotResponse(
        String kpiCode,
        String kpiName,
        String sourceSystem,
        int affectedRecords,
        BigDecimal value,
        OffsetDateTime computedAt
) {}
