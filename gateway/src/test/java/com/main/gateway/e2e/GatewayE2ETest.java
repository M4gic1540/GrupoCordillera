package com.main.gateway.e2e;

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
class GatewayE2ETest {

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

    @ParameterizedTest(name = "E2E flujo completo #{index}")
    @MethodSource("e2eFlows")
    void shouldExecuteGatewayE2EFlow(String path, boolean requiresToken) {
        WebTestClient.RequestHeadersSpec<?> request = client().get().uri(path);
        if (requiresToken) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer valid-e2e-token");
        }

        request.exchange().expectStatus().isOk();
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> e2eFlows() {
        Stream<org.junit.jupiter.params.provider.Arguments> publicFlows = IntStream.range(0, 8)
                .mapToObj(i -> org.junit.jupiter.params.provider.Arguments.of("/api/auth/login?flow=" + i, false));

        Stream<org.junit.jupiter.params.provider.Arguments> privateFlows = IntStream.range(0, 7)
                .mapToObj(i -> org.junit.jupiter.params.provider.Arguments.of(i % 2 == 0 ? "/api/ingestion/e2e/" + i : "/api/kpi/e2e/" + i, true));

        return Stream.concat(publicFlows, privateFlows);
    }
}
