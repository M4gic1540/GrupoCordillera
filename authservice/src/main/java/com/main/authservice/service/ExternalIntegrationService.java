package com.main.authservice.service;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import com.main.authservice.external.ConnectorFactory;
import com.main.authservice.external.ExternalConnector;

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
    private final CircuitBreaker externalCallsCircuitBreaker;

    public ExternalIntegrationService(ExternalIntegrationsProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();


        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getFailureRateThreshold())
                .slidingWindowSize(properties.getSlidingWindowSize())
                .minimumNumberOfCalls(properties.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(properties.getPermittedCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofMillis(properties.getWaitDurationOpenStateMs()))
                .build();

        this.externalCallsCircuitBreaker = CircuitBreaker.of("auth-external-calls", circuitBreakerConfig);
    }

    public void notifyUserRegistered(User user, String connectorType) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            externalCallsCircuitBreaker.executeRunnable(() -> doNotifyUserRegistered(user, connectorType));
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

    private void doNotifyUserRegistered(User user, String connectorType) {
        ExternalConnector connector = ConnectorFactory.createConnector(connectorType);
        logger.info("Usando conector externo tipo {} para userId={}", connector.getType(), user.getId());
        connector.connect();
        // Aquí podrías llamar métodos como connector.sendData(user) si los defines en la interfaz
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
