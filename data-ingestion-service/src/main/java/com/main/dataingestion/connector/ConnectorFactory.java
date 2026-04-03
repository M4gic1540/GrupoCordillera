package com.main.dataingestion.connector;

import com.main.dataingestion.config.SourceSystemsProperties;
import com.main.dataingestion.config.SourceSystemsProperties.SourceSystemConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ConnectorFactory {

    private final Map<String, SourceConnector> connectors;

    public ConnectorFactory(SourceSystemsProperties properties,
                            WebClient.Builder webClientBuilder,
                            CircuitBreakerRegistry cbRegistry) {
        this.connectors = toMap(properties, webClientBuilder, cbRegistry);
    }

    public SourceConnector getConnector(String sourceKey) {
        SourceConnector connector = connectors.get(sourceKey);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown source system: " + sourceKey);
        }
        return connector;
    }

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
