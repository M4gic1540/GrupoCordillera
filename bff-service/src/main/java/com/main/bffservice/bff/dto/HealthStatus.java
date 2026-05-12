package com.main.bffservice.bff.dto;

import java.util.Map;

public record HealthStatus(String service, String status, Map<String, Object> details) {
    public static HealthStatus down(String service) {
        return new HealthStatus(service, "DOWN", Map.of());
    }
}
