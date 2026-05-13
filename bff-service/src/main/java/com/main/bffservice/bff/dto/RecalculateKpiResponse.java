package com.main.bffservice.bff.dto;

/*
 * RecalculateKpiResponse - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO, BFF
 */


import java.util.List;

public record RecalculateKpiResponse(
        String sourceSystem,
        int affectedRecords,
        List<KpiSnapshotResponse> snapshots
) {}
