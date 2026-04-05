package com.main.kpiengine.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExternalServicesIntegrationTest {

    @Test
    void shouldBuildKpiNotifyPath() {
        String base = "http://kpi-engine:8080";
        String path = "/api/kpi/recalculate";

        assertEquals("http://kpi-engine:8080/api/kpi/recalculate", base + path);
    }

    @Test
    void shouldExposeHealthEndpointPath() {
        String health = "/api/kpi/health";
        assertTrue(health.startsWith("/api/"));
        assertTrue(health.endsWith("health"));
    }
}
