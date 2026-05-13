
// Paquete raÃ­z gateway
package com.main.gateway;

/*
 * GatewayApplication - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */



// Importa Spring Boot launcher
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


// Marca clase como app Spring Boot
@SpringBootApplication
public class GatewayApplication {

    // Entry point. Arranca microservicio gateway
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
