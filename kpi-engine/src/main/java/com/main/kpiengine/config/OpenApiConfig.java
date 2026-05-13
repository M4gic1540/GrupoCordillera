package com.main.kpiengine.config;

/*
 * OpenApiConfig - Config.
 * Responsibilities: Configuracion de beans y propiedades externas.
 * Patterns: Configuration
 */


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ConfiguraciÃ³n de metadatos OpenAPI para documentaciÃ³n Swagger.
 *
 * <p>Define tÃ­tulo, descripciÃ³n y versiÃ³n visibles en UI y contrato generado.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Registra objeto OpenAPI base para el microservicio KPI Engine.
     *
     * @return definiciÃ³n OpenAPI con informaciÃ³n general de la API.
     */
    @Bean
    public OpenAPI kpiOpenApi() {
        return new OpenAPI().info(
                new Info()
                        .title("KPI Engine API")
                        .description("API para recalculo y consulta de indicadores")
                        .version("v1")
        );
    }
}
