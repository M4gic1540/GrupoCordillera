package com.main.dataingestion.config;

/*
 * WebClientConfig - Config.
 * Responsibilities: Configuracion de beans y propiedades externas.
 * Patterns: Configuration
 */


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
