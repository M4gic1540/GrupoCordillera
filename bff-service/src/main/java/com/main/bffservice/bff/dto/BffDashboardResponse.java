package com.main.bffservice.bff.dto;

import java.util.List;

public class BffDashboardResponse {

    private final HealthStatus ingestionHealth;
    private final HealthStatus kpiHealth;
    private final HealthStatus authHealth;
    private final List<KpiSnapshotResponse> latestKpiSnapshots;

    public BffDashboardResponse(
            HealthStatus ingestionHealth,
            HealthStatus kpiHealth,
            HealthStatus authHealth,
            List<KpiSnapshotResponse> latestKpiSnapshots
    ) {
        this.ingestionHealth = ingestionHealth;
        this.kpiHealth = kpiHealth;
        this.authHealth = authHealth;
        this.latestKpiSnapshots = latestKpiSnapshots;
    }

    public HealthStatus getIngestionHealth() {
        return ingestionHealth;
    }

    public HealthStatus getKpiHealth() {
        return kpiHealth;
    }

    public HealthStatus getAuthHealth() {
        return authHealth;
    }

    public List<KpiSnapshotResponse> getLatestKpiSnapshots() {
        return latestKpiSnapshots;
    }
}

