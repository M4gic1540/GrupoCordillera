package com.main.kpiengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio KPI Engine.
 *
 * <p>Arranca el contexto Spring Boot y habilita autoconfiguración de
 * componentes (web, JPA, validación, etc.) para el servicio de KPIs.</p>
 */
@SpringBootApplication
public class KpiEngineApplication {

    /**
     * Método principal de arranque de la aplicación.
     *
     * @param args argumentos de línea de comandos del proceso Java.
     */
    public static void main(String[] args) {
        SpringApplication.run(KpiEngineApplication.class, args);
    }
}
