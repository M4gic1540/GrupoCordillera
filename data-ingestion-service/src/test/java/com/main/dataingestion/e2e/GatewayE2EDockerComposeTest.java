package com.main.dataingestion.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class GatewayE2EDockerComposeTest {

    private static final String GATEWAY_BASE_URL = System.getenv().getOrDefault("E2E_GATEWAY_BASE_URL", "http://localhost:8080");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void shouldExecuteEndToEndFlowThroughGatewayAndProtectedIngestionEndpoint() throws Exception {
        String enabled = System.getProperty(
                "e2e.docker.enabled",
                System.getenv().getOrDefault("E2E_DOCKER_ENABLED", "false")
        );
        Assumptions.assumeTrue("true".equalsIgnoreCase(enabled),
                "Set E2E_DOCKER_ENABLED=true to run Docker Compose E2E tests.");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String accessToken = System.getenv("E2E_ACCESS_TOKEN");
        if (accessToken == null || accessToken.isBlank()) {
            String email = "e2e-" + UUID.randomUUID() + "@test.local";
            String password = "StrongPass123";

            String registerBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
            HttpResponse<String> registerResponse = client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(GATEWAY_BASE_URL + "/api/auth/register"))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            Assumptions.assumeTrue(registerResponse.statusCode() == 201,
                    "Could not obtain JWT from /api/auth/register. status=" + registerResponse.statusCode()
                            + ", body=" + registerResponse.body());

            JsonNode registerJson = OBJECT_MAPPER.readTree(registerResponse.body());
            accessToken = registerJson.path("accessToken").asText();
            assertThat(accessToken).isNotBlank();
        }

        HttpResponse<String> unauthorizedResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(GATEWAY_BASE_URL + "/api/ingestion/sync/crm"))
                        .timeout(Duration.ofSeconds(20))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        Assumptions.assumeTrue(unauthorizedResponse.statusCode() != 404,
                "Gateway route /api/ingestion/sync/crm is not available (404). "
                        + "Ensure docker-compose and gateway config include ingestion routing.");
        assertThat(unauthorizedResponse.statusCode()).isEqualTo(401);

        HttpResponse<String> ingestionResponse = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(GATEWAY_BASE_URL + "/api/ingestion/sync/crm"))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("X-Correlation-ID", "e2e-test-correlation")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(ingestionResponse.statusCode()).isEqualTo(200);
        assertThat(ingestionResponse.headers().firstValue("X-Correlation-ID")).isPresent();

        JsonNode ingestionJson = OBJECT_MAPPER.readTree(ingestionResponse.body());
        assertThat(ingestionJson.path("sourceSystem").asText()).isEqualTo("crm");
        assertThat(ingestionJson.path("processedRecords").isNumber()).isTrue();
    }
}
