// Paquete config: centraliza configuración gateway
package com.main.gateway.config;


// Importa anotaciones Spring y dependencias de propiedades
import java.util.Arrays;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.gateway.security.GatewaySecurityProperties;


// Marca clase como configuración Spring
@Configuration
// Habilita inyección de propiedades de seguridad
@EnableConfigurationProperties({GatewaySecurityProperties.class})
public class GatewayConfig {

    // Bean WebClient: usado para llamadas HTTP internas (ej: validar JWT, llamar otros servicios)
    @Bean
    public WebClient gatewayWebClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Permitimos el origen del frontend explícitamente. 
        // Nota: Con allowCredentials(true), no se puede usar "*" en origins.
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
