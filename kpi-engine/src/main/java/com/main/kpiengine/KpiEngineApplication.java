package com.main.kpiengine;

/*
 * KpiEngineApplication - Componente.
 * Responsibilities: Logica principal del modulo.
 * Patterns: N/A
 */


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio KPI Engine.
 *
 * <p>Arranca el contexto Spring Boot y habilita autoconfiguraciÃ³n de
 * componentes (web, JPA, validaciÃ³n, etc.) para el servicio de KPIs.</p>
 */
@SpringBootApplication
public class KpiEngineApplication {

    /**
     * MÃ©todo principal de arranque de la aplicaciÃ³n.
     *
     * @param args argumentos de lÃ­nea de comandos del proceso Java.
     */
    public static void main(String[] args) {
        SpringApplication.run(KpiEngineApplication.class, args);
    }
}
