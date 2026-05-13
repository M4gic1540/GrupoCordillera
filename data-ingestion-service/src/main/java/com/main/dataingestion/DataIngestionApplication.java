package com.main.dataingestion;

/*
 * DataIngestionApplication - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */


import com.main.dataingestion.config.SourceSystemsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SourceSystemsProperties.class)
public class DataIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataIngestionApplication.class, args);
    }
}

