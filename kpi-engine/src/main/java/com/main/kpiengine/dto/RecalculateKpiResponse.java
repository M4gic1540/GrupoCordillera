package com.main.kpiengine.dto;

import java.util.List;

public record RecalculateKpiResponse(
        String sourceSystem,
        int affectedRecords,
        List<KpiSnapshotResponse> snapshots
) {
}
