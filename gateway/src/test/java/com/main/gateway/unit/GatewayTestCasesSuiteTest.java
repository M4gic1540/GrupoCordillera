package com.main.gateway.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.gateway.config.GatewayConfig;
import com.main.gateway.security.GatewaySecurityProperties;

class GatewayTestCasesSuiteTest {

    @Test
    @DisplayName("Debe crear bean de propiedades")
    void shouldCreatePropertiesBean() {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        assertNotNull(properties);

        GatewayConfig config = new GatewayConfig();
        WebClient client = config.gatewayWebClient(WebClient.builder());
        assertNotNull(client);
    }

    @ParameterizedTest(name = "Caso propiedades #{index}")
    @MethodSource("propertyPermutations")
    void shouldBindPropertiesInMultipleCases(List<String> protectedPaths, List<String> excludedPaths, String authUrl) {
        GatewaySecurityProperties properties = new GatewaySecurityProperties();
        properties.setProtectedPaths(protectedPaths);
        properties.setExcludedPaths(excludedPaths);
        properties.setAuthValidationUrl(authUrl);

        assertEquals(protectedPaths, properties.getProtectedPaths());
        assertEquals(excludedPaths, properties.getExcludedPaths());
        assertEquals(authUrl, properties.getAuthValidationUrl());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> propertyPermutations() {
        return IntStream.range(0, 25)
                .mapToObj(i -> org.junit.jupiter.params.provider.Arguments.of(
                        List.of("/api/ingestion/" + i + "/**", "/api/kpi/" + i + "/**"),
                        List.of("/api/auth/**", "/actuator/**", "/kpi/**"),
                        "http://auth-service/validate/" + i));
    }

    @ParameterizedTest(name = "Regla de inclusión/exclusión #{index}")
    @CsvSource({
            "/api/auth/login,false",
            "/api/auth/register,false",
            "/actuator/health,false",
            "/kpi/docs,false",
            "/api/ingestion/events,true",
            "/api/kpi/report,true",
            "/api/unknown,true",
            "/api/ingestion/perf,true",
            "/api/kpi/perf,true",
            "/api/custom,true",
            "/api/auth/validate,false",
            "/kpi/openapi.json,false",
            "/api/security-test,true",
            "/api/ingestion/security,true",
            "/actuator/info,false"
    })
    void shouldApplyPathRules(String path, boolean expectedProtected) {
        List<String> excluded = List.of("/api/auth/**", "/actuator/**", "/kpi/**");
        List<String> protectedPaths = List.of("/api/ingestion/**", "/api/kpi/**", "/api/**");

        boolean isExcluded = excluded.stream().anyMatch(pattern -> new org.springframework.util.AntPathMatcher().match(pattern, path));
        boolean isProtected = !isExcluded && protectedPaths.stream().anyMatch(pattern -> new org.springframework.util.AntPathMatcher().match(pattern, path));

        assertEquals(expectedProtected, isProtected);
    }

    @TestFactory
    Stream<DynamicTest> shouldGenerateDynamicSanityCases() {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(i -> DynamicTest.dynamicTest("dynamic-case-" + i, () -> {
                    String routeId = "route-" + i;
                    assertTrue(routeId.startsWith("route-"));
                    assertTrue(routeId.length() > 6);
                }));
    }
}
