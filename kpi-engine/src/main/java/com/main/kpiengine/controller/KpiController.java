package com.main.kpiengine.controller;

import com.main.kpiengine.dto.KpiSnapshotResponse;
import com.main.kpiengine.dto.RecalculateKpiRequest;
import com.main.kpiengine.dto.RecalculateKpiResponse;
import com.main.kpiengine.service.KpiEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST principal del motor KPI.
 *
 * <p>Expone operaciones de recálculo y consulta de snapshots para clientes
 * internos (gateway/BFF u otros servicios).</p>
 */
@RestController
@RequestMapping("/api/kpi")
@Tag(name = "KPI", description = "Endpoints para recalculo y consulta de indicadores")
public class KpiController {

    private final KpiEngineService kpiEngineService;

    public KpiController(KpiEngineService kpiEngineService) {
        this.kpiEngineService = kpiEngineService;
    }

    /**
     * Dispara recálculo de KPIs para una fuente y volumen afectados.
     *
     * @param request payload validado con origen y cantidad de registros.
     * @return respuesta con snapshots recién calculados.
     */
    @PostMapping("/recalculate")
    @Operation(summary = "Recalcular KPI", description = "Calcula snapshots de KPI para una fuente de datos afectada.")
    public ResponseEntity<RecalculateKpiResponse> recalculate(@Valid @RequestBody RecalculateKpiRequest request) {
        return ResponseEntity.ok(kpiEngineService.recalculate(request));
    }

    /**
     * Recupera los últimos snapshots calculados por el motor KPI.
     *
     * @return lista ordenada de snapshots recientes.
     */
    @GetMapping("/snapshots/latest")
    @Operation(summary = "Ultimos snapshots", description = "Obtiene los ultimos snapshots calculados por el motor KPI.")
    public ResponseEntity<List<KpiSnapshotResponse>> getLatestSnapshots() {
        return ResponseEntity.ok(kpiEngineService.getLatestSnapshots());
    }
}
