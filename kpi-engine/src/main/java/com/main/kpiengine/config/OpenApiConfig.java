package com.main.kpiengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de metadatos OpenAPI para documentación Swagger.
 *
 * <p>Define título, descripción y versión visibles en UI y contrato generado.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Registra objeto OpenAPI base para el microservicio KPI Engine.
     *
     * @return definición OpenAPI con información general de la API.
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
