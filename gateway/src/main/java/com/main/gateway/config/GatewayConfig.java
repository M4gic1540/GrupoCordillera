
// Paquete config: centraliza configuración gateway
package com.main.gateway.config;


// Importa anotaciones Spring y dependencias de propiedades
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.gateway.bff.config.BffProperties;
import com.main.gateway.security.GatewaySecurityProperties;


// Marca clase como configuración Spring
@Configuration
// Habilita inyección de propiedades de seguridad y BFF
@EnableConfigurationProperties({GatewaySecurityProperties.class, BffProperties.class})
public class GatewayConfig {

    // Bean WebClient: usado para llamadas HTTP internas (ej: validar JWT, llamar otros servicios)
    @Bean
    public WebClient gatewayWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
