package com.main.dataingestion.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.main.dataingestion.config.SourceSystemsProperties.SourceSystemConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class HttpSourceConnector implements SourceConnector {

    private static final Logger logger = LoggerFactory.getLogger(HttpSourceConnector.class);

    private final SourceSystemConfig config;
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public HttpSourceConnector(SourceSystemConfig config, WebClient.Builder webClientBuilder, CircuitBreakerRegistry cbRegistry) {
        this.config = config;
        this.webClient = webClientBuilder.baseUrl(config.getBaseUrl()).build();
        this.circuitBreaker = cbRegistry.circuitBreaker(config.getCircuitBreakerName());
    }

    @Override
    public String sourceKey() {
        return config.getKey();
    }

    @Override
    public List<JsonNode> fetchBatch() {
        return circuitBreaker.executeSupplier(this::doFetch);
    }

    private List<JsonNode> doFetch() {
        try {
            JsonNode response = webClient.get()
                    .uri(config.getEndpointPath())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .block();

            if (response == null) {
                return Collections.emptyList();
            }

            if (response.isArray()) {
                List<JsonNode> items = new ArrayList<>();
                response.forEach(items::add);
                return items;
            }

            return List.of(response);
        } catch (WebClientResponseException ex) {
            logger.warn("Source {} returned status {}", config.getKey(), ex.getStatusCode());
            throw ex;
        } catch (Exception ex) {
            logger.warn("Source {} unavailable, applying fallback", config.getKey());
            return Collections.emptyList();
        }
    }
}
