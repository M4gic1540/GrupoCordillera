package com.main.bffservice.config;

/*
 * BffConfig - Config.
 * Responsibilities: Configuracion de beans y propiedades externas.
 * Patterns: Configuration
 */


import com.main.bffservice.bff.config.BffProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(BffProperties.class)
public class BffConfig {

    @Bean
    public WebClient bffWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}

