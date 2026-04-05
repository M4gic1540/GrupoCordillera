package com.main.authservice.actuator;

import com.main.authservice.service.ExternalIntegrationService;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "externalCircuitBreaker")
public class ExternalCircuitBreakerEndpoint {

    private final ExternalIntegrationService externalIntegrationService;

    public ExternalCircuitBreakerEndpoint(ExternalIntegrationService externalIntegrationService) {
        this.externalIntegrationService = externalIntegrationService;
    }

    @ReadOperation
    public Map<String, Object> status() {
        return externalIntegrationService.getCircuitBreakerStatus();
    }
}
