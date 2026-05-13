package com.main.dataingestion.controller;

/*
 * IngestionController - Controller REST.
 * Responsibilities: Punto de entrada HTTP y validacion de requests.
 * Patterns: MVC
 */


import com.main.dataingestion.service.IngestionService;
import com.main.dataingestion.service.IngestionService.SyncResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/ingestion")
@Tag(name = "Ingestion", description = "Endpoints para sincronizacion de datos")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/sync/{sourceSystem}")
    @Operation(summary = "Sincronizar fuente", description = "Ingiere eventos desde un sistema fuente y dispara notificacion KPI")
    public ResponseEntity<SyncResult> syncSource(
            @Parameter(description = "Clave del sistema fuente (ej: crm, erp, hr)")
            @PathVariable
            @Pattern(regexp = "^[a-z][a-z0-9-]{1,39}$", message = "sourceSystem has invalid format")
            String sourceSystem) {
        return ResponseEntity.ok(ingestionService.ingest(sourceSystem));
    }
}
