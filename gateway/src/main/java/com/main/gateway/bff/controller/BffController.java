
// Paquete controller: expone endpoints BFF para frontend
package com.main.gateway.bff.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.gateway.bff.dto.BffDashboardResponse;
import com.main.gateway.bff.service.BffService;

import reactor.core.publisher.Mono;


// Marca clase como controlador REST
@RestController
// Prefijo rutas BFF
@RequestMapping("/bff")
public class BffController {

    // Servicio de lógica BFF
    private final BffService bffService;

    // Constructor inyecta servicio
    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    // Endpoint GET /bff/dashboard: responde dashboard agregado para frontend
    @GetMapping("/dashboard")
    public Mono<ResponseEntity<BffDashboardResponse>> getDashboard() {
        return bffService.getDashboard().map(ResponseEntity::ok);
    }
}
