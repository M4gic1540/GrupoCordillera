
// Paquete raíz gateway
package com.main.gateway;


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
