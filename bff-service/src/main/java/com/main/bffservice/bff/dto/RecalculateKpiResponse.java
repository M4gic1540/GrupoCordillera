package com.main.bffservice.bff.dto;

import java.util.List;

public record RecalculateKpiResponse(
        String sourceSystem,
        int affectedRecords,
        List<KpiSnapshotResponse> snapshots
) {}
