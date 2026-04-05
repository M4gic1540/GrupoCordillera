package com.main.authservice.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.authservice.config.ExternalIntegrationsProperties;
import com.main.authservice.model.User;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Service
public class ExternalIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalIntegrationService.class);

    private final ExternalIntegrationsProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final CircuitBreaker externalCallsCircuitBreaker;

    public ExternalIntegrationService(ExternalIntegrationsProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getFailureRateThreshold())
                .slidingWindowSize(properties.getSlidingWindowSize())
                .minimumNumberOfCalls(properties.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(properties.getPermittedCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofMillis(properties.getWaitDurationOpenStateMs()))
                .build();

        this.externalCallsCircuitBreaker = CircuitBreaker.of("auth-external-calls", circuitBreakerConfig);
    }

    public void notifyUserRegistered(User user) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            externalCallsCircuitBreaker.executeRunnable(() -> doNotifyUserRegistered(user));
        } catch (CallNotPermittedException ex) {
            logger.warn("Circuit breaker abierto para integraciones externas, se omite notificación de registro userId={}", user.getId());
        } catch (Exception ex) {
            logger.warn("Fallo en integración externa post-registro para userId={}: {}", user.getId(), ex.getMessage());
        }
    }

    public Map<String, Object> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("name", externalCallsCircuitBreaker.getName());
        status.put("state", externalCallsCircuitBreaker.getState().name());
        status.put("failureRate", externalCallsCircuitBreaker.getMetrics().getFailureRate());
        status.put("bufferedCalls", externalCallsCircuitBreaker.getMetrics().getNumberOfBufferedCalls());
        status.put("failedCalls", externalCallsCircuitBreaker.getMetrics().getNumberOfFailedCalls());
        status.put("successfulCalls", externalCallsCircuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        return status;
    }

    private void doNotifyUserRegistered(User user) {
        URI endpoint = URI.create(properties.getBaseUrl() + properties.getUserRegisteredPath());

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(buildPayload(user)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Integración externa de registro ejecutada para userId={} status={}", user.getId(), statusCode);
                return;
            }
            throw new IllegalStateException("External endpoint returned status=" + statusCode);
        } catch (Exception ex) {
            throw new RuntimeException("No fue posible notificar registro a integraciones externas", ex);
        }
    }

    private String buildPayload(User user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "USER_REGISTERED");
        payload.put("userId", user.getId());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole().name());
        payload.put("createdAt", user.getCreatedAt());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible serializar payload de integración", ex);
        }
    }
}
