package com.main.dataingestion.controller;

import com.main.dataingestion.service.IngestionService;
import com.main.dataingestion.service.IngestionService.SyncResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/sync/{sourceSystem}")
    public ResponseEntity<SyncResult> syncSource(@PathVariable String sourceSystem) {
        return ResponseEntity.ok(ingestionService.ingest(sourceSystem));
    }
}
