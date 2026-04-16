
// Paquete dto: define contratos de datos para respuestas BFF
package com.main.gateway.bff.dto;


import java.util.List;
import java.util.Map;


// DTO para respuesta agregada del dashboard BFF
public class BffDashboardResponse {

    // Estado health de data-ingestion-service
    private final Map<String, Object> ingestionHealth;
    // Estado health de kpi-engine
    private final Map<String, Object> kpiHealth;
    // Lista de snapshots KPI más recientes
    private final List<Map<String, Object>> latestKpiSnapshots;

    // Constructor: recibe los tres componentes agregados
    public BffDashboardResponse(
            Map<String, Object> ingestionHealth,
            Map<String, Object> kpiHealth,
            List<Map<String, Object>> latestKpiSnapshots
    ) {
        this.ingestionHealth = ingestionHealth;
        this.kpiHealth = kpiHealth;
        this.latestKpiSnapshots = latestKpiSnapshots;
    }

    // Getter estado health de ingesta
    public Map<String, Object> getIngestionHealth() {
        return ingestionHealth;
    }

    // Getter estado health de KPI
    public Map<String, Object> getKpiHealth() {
        return kpiHealth;
    }

    // Getter lista de snapshots KPI
    public List<Map<String, Object>> getLatestKpiSnapshots() {
        return latestKpiSnapshots;
    }
}
