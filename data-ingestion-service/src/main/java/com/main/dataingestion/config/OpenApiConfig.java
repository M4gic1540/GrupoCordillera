package com.main.dataingestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dataIngestionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Data Ingestion Service API")
                        .description("API para sincronizacion de eventos desde sistemas externos")
                        .version("v1")
                        .contact(new Contact().name("Grupo Cordillera").email("soporte@grupocordillera.local"))
                        .license(new License().name("Internal Use").url("https://grupocordillera.local")));
    }
}
