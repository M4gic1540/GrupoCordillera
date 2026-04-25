package com.main.gateway.performance;

import java.time.Duration;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToServer;

import com.main.gateway.testutil.GatewayHttpStubs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayPerformanceTest {

    private static final GatewayHttpStubs STUBS = GatewayHttpStubs.start();

    @LocalServerPort
    private int port;

    @AfterAll
    static void tearDownServers() {
        STUBS.stop();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("gateway.security.auth-validation-url", STUBS::authValidationUrl);
        registry.add("AUTHSERVICE_1_URL", STUBS::authService1Url);
        registry.add("AUTHSERVICE_2_URL", STUBS::authService2Url);
        registry.add("INGESTION_1_URL", STUBS::ingestion1Url);
        registry.add("INGESTION_2_URL", STUBS::ingestion2Url);
        registry.add("KPI_ENGINE_URL", STUBS::kpiUrl);
    }

    private WebTestClient client() {
        return bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(10)).build();
    }

    @ParameterizedTest(name = "Performance request #{index}")
    @MethodSource("performancePaths")
    void shouldRespondUnderLatencyThreshold(String path) {
        long start = System.nanoTime();
        client().get()
            .uri(path)
            .header(HttpHeaders.AUTHORIZATION, "Bearer perf-token")
            .exchange()
            .expectStatus().isOk();

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        long threshold = 2000;
        String env = System.getenv("GATEWAY_LATENCY_THRESHOLD");
        if (env != null) {
            try {
            threshold = Long.parseLong(env);
            } catch (NumberFormatException ignored) {}
        }
        assertTrue(elapsedMillis < threshold, "Gateway latency exceeded threshold: " + elapsedMillis + "ms (threshold: " + threshold + "ms)");
    }

    static Stream<String> performancePaths() {
        return IntStream.range(0, 15)
                .mapToObj(i -> i % 2 == 0 ? "/api/ingestion/performance/" + i : "/api/kpi/performance/" + i);
    }
}
