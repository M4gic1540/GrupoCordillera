package com.main.dataingestion.connector;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.dataingestion.config.SourceSystemsProperties;
import com.main.dataingestion.config.SourceSystemsProperties.SourceSystemConfig;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@Component
public class ConnectorFactory {

    private final Map<String, SourceConnector> connectors;

    public ConnectorFactory(SourceSystemsProperties properties,
                            WebClient.Builder webClientBuilder,
                            CircuitBreakerRegistry cbRegistry) {
        this.connectors = toMap(properties, webClientBuilder, cbRegistry);
    }

    /**
     * Devuelve el conector asociado a una clave de sistema fuente.
     *
     * @param sourceKey clave configurada en application.yml (ej. crm, erp).
     * @return conector listo para consumir eventos.
     * @throws IllegalArgumentException si la fuente no está registrada.
     */
    public SourceConnector getConnector(String sourceKey) {
        SourceConnector connector = connectors.get(sourceKey);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown source system: " + sourceKey);
        }
        return connector;
    }

    /**
     * Construye el registro in-memory de conectores al arranque.
     */
    private Map<String, SourceConnector> toMap(SourceSystemsProperties properties,
                                               WebClient.Builder webClientBuilder,
                                               CircuitBreakerRegistry cbRegistry) {
        Map<String, SourceConnector> map = new HashMap<>();
        for (SourceSystemConfig cfg : properties.getSystems()) {
            map.put(cfg.getKey(), new HttpSourceConnector(cfg, webClientBuilder, cbRegistry));
        }
        return map;
    }
}
