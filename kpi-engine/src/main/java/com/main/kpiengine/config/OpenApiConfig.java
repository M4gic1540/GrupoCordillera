package com.main.kpiengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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
