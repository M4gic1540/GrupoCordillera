package com.main.gateway.security;

import static org.springframework.test.web.reactive.server.WebTestClient.bindToServer;

import java.time.Duration;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.main.gateway.testutil.GatewayHttpStubs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTest {

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
        return bindToServer().baseUrl("http://localhost:" + port).responseTimeout(Duration.ofSeconds(5)).build();
    }

    @ParameterizedTest(name = "Seguridad sin token #{index}")
    @MethodSource("protectedPaths")
    void shouldRejectProtectedRequestsWithoutToken(String path) {
        client().get()
                .uri(path)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest(name = "Seguridad token invalido #{index}")
    @MethodSource("protectedPaths")
    void shouldRejectProtectedRequestsWithInvalidToken(String path) {
        client().get()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest(name = "Seguridad rutas publicas #{index}")
    @MethodSource("publicPaths")
    void shouldAllowPublicPathsWithoutToken(String path) {
        client().get()
                .uri(path)
                .exchange()
                .expectStatus().isOk();
    }

    static Stream<String> protectedPaths() {
        return IntStream.range(0, 5)
                .mapToObj(i -> i % 2 == 0 ? "/api/ingestion/security/" + i : "/api/kpi/security/" + i);
    }

    static Stream<String> publicPaths() {
        return IntStream.range(0, 5)
                .mapToObj(i -> i % 2 == 0 ? "/api/auth/login?security=" + i : "/kpi/docs");
    }
}
