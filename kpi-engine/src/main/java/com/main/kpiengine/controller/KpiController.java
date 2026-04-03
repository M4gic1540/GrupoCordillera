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

@RestController
@RequestMapping("/api/kpi")
@Tag(name = "KPI", description = "Endpoints para recalculo y consulta de indicadores")
public class KpiController {

    private final KpiEngineService kpiEngineService;

    public KpiController(KpiEngineService kpiEngineService) {
        this.kpiEngineService = kpiEngineService;
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalcular KPI", description = "Calcula snapshots de KPI para una fuente de datos afectada.")
    public ResponseEntity<RecalculateKpiResponse> recalculate(@Valid @RequestBody RecalculateKpiRequest request) {
        return ResponseEntity.ok(kpiEngineService.recalculate(request));
    }

    @GetMapping("/snapshots/latest")
    @Operation(summary = "Ultimos snapshots", description = "Obtiene los ultimos snapshots calculados por el motor KPI.")
    public ResponseEntity<List<KpiSnapshotResponse>> getLatestSnapshots() {
        return ResponseEntity.ok(kpiEngineService.getLatestSnapshots());
    }
}
