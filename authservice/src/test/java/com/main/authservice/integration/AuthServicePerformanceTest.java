package com.main.authservice.integration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("local")
@Tag("performance")
class AuthServicePerformanceTest {

    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASURE_ITERATIONS = 20;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity(springSecurityFilterChain))
                .build();
    }

    @Test
    void registerShouldKeepP95Under700ms() throws Exception {
        LatencyStats stats = measureRegisterLatency(MEASURE_ITERATIONS);
        assertTrue(stats.p95Ms <= 700, "register p95 is too high: " + stats.p95Ms + " ms");
    }

    @Test
    void loginShouldKeepP95Under600ms() throws Exception {
        LatencyStats stats = measureLoginLatency(MEASURE_ITERATIONS);
        assertTrue(stats.p95Ms <= 600, "login p95 is too high: " + stats.p95Ms + " ms");
    }

    @Test
    void refreshShouldKeepP95Under650ms() throws Exception {
        LatencyStats stats = measureRefreshLatency(MEASURE_ITERATIONS);
        assertTrue(stats.p95Ms <= 650, "refresh p95 is too high: " + stats.p95Ms + " ms");
    }

    @Test
    void validateShouldKeepP95Under400ms() throws Exception {
        LatencyStats stats = measureValidateLatency(MEASURE_ITERATIONS);
        assertTrue(stats.p95Ms <= 400, "validate p95 is too high: " + stats.p95Ms + " ms");
    }

    @Test
    void meShouldKeepP95Under500ms() throws Exception {
        LatencyStats stats = measureMeLatency(MEASURE_ITERATIONS);
        assertTrue(stats.p95Ms <= 500, "me p95 is too high: " + stats.p95Ms + " ms");
    }

    @Test
    void registerBatchShouldCompleteUnder12s() throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < 30; i++) {
            String email = uniqueEmail("perf-batch-reg-" + i);
            register(email, "Password123");
        }
        long elapsedMs = toMs(System.nanoTime() - start);
        assertTrue(elapsedMs <= 12000, "register batch is too slow: " + elapsedMs + " ms");
    }

    @Test
    void loginBatchShouldCompleteUnder10s() throws Exception {
        List<String> users = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            String email = uniqueEmail("perf-batch-login-" + i);
            register(email, "Password123");
            users.add(email);
        }

        long start = System.nanoTime();
        for (String email : users) {
            login(email, "Password123");
        }
        long elapsedMs = toMs(System.nanoTime() - start);
        assertTrue(elapsedMs <= 10000, "login batch is too slow: " + elapsedMs + " ms");
    }

    @Test
    void refreshBatchShouldCompleteUnder10s() throws Exception {
        List<String> refreshTokens = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String email = uniqueEmail("perf-batch-refresh-" + i);
            JsonNode registerJson = objectMapper.readTree(register(email, "Password123"));
            refreshTokens.add(registerJson.get("refreshToken").asText());
        }

        long start = System.nanoTime();
        for (String refreshToken : refreshTokens) {
            refresh(refreshToken);
        }
        long elapsedMs = toMs(System.nanoTime() - start);
        assertTrue(elapsedMs <= 10000, "refresh batch is too slow: " + elapsedMs + " ms");
    }

    @Test
    void endToEndAuthFlowAverageShouldStayUnder700ms() throws Exception {
        List<Long> flowTimes = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            String email = uniqueEmail("perf-flow-" + i);
            long start = System.nanoTime();

            JsonNode registerJson = objectMapper.readTree(register(email, "Password123"));
            String accessToken = registerJson.get("accessToken").asText();
            JsonNode loginJson = objectMapper.readTree(login(email, "Password123"));
            String refreshToken = loginJson.get("refreshToken").asText();
            refresh(refreshToken);
            validate(accessToken);
            me(accessToken);

            flowTimes.add(toMs(System.nanoTime() - start));
        }

        long average = (long) flowTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        assertTrue(average <= 700, "e2e auth flow average is too high: " + average + " ms");
    }

    @Test
    void warmApplicationFlowShouldStayUnder2s() throws Exception {
        String email = uniqueEmail("perf-warm");

        register(email, "Password123");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            login(email, "Password123");
        }

        long start = System.nanoTime();
        login(email, "Password123");
        long elapsedMs = toMs(System.nanoTime() - start);

        assertTrue(elapsedMs <= 2000, "warm login is too slow: " + elapsedMs + " ms");
    }

    private LatencyStats measureRegisterLatency(int iterations) throws Exception {
        List<Long> values = new ArrayList<>();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            register(uniqueEmail("perf-warm-reg-" + i), "Password123");
        }

        for (int i = 0; i < iterations; i++) {
            String email = uniqueEmail("perf-reg-" + i);
            long start = System.nanoTime();
            register(email, "Password123");
            values.add(toMs(System.nanoTime() - start));
        }

        return computeStats(values);
    }

    private LatencyStats measureLoginLatency(int iterations) throws Exception {
        List<Long> values = new ArrayList<>();

        List<String> users = new ArrayList<>();
        for (int i = 0; i < iterations + WARMUP_ITERATIONS; i++) {
            String email = uniqueEmail("perf-login-" + i);
            register(email, "Password123");
            users.add(email);
        }

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            login(users.get(i), "Password123");
        }

        for (int i = WARMUP_ITERATIONS; i < users.size(); i++) {
            long start = System.nanoTime();
            login(users.get(i), "Password123");
            values.add(toMs(System.nanoTime() - start));
        }

        return computeStats(values);
    }

    private LatencyStats measureRefreshLatency(int iterations) throws Exception {
        List<Long> values = new ArrayList<>();

        List<String> refreshTokens = new ArrayList<>();
        for (int i = 0; i < iterations + WARMUP_ITERATIONS; i++) {
            String email = uniqueEmail("perf-refresh-" + i);
            JsonNode registerJson = objectMapper.readTree(register(email, "Password123"));
            refreshTokens.add(registerJson.get("refreshToken").asText());
        }

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            refresh(refreshTokens.get(i));
        }

        for (int i = WARMUP_ITERATIONS; i < refreshTokens.size(); i++) {
            long start = System.nanoTime();
            refresh(refreshTokens.get(i));
            values.add(toMs(System.nanoTime() - start));
        }

        return computeStats(values);
    }

    private LatencyStats measureValidateLatency(int iterations) throws Exception {
        List<Long> values = new ArrayList<>();

        List<String> accessTokens = new ArrayList<>();
        for (int i = 0; i < iterations + WARMUP_ITERATIONS; i++) {
            String email = uniqueEmail("perf-validate-" + i);
            JsonNode registerJson = objectMapper.readTree(register(email, "Password123"));
            accessTokens.add(registerJson.get("accessToken").asText());
        }

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            validate(accessTokens.get(i));
        }

        for (int i = WARMUP_ITERATIONS; i < accessTokens.size(); i++) {
            long start = System.nanoTime();
            validate(accessTokens.get(i));
            values.add(toMs(System.nanoTime() - start));
        }

        return computeStats(values);
    }

    private LatencyStats measureMeLatency(int iterations) throws Exception {
        List<Long> values = new ArrayList<>();

        List<String> accessTokens = new ArrayList<>();
        for (int i = 0; i < iterations + WARMUP_ITERATIONS; i++) {
            String email = uniqueEmail("perf-me-" + i);
            JsonNode registerJson = objectMapper.readTree(register(email, "Password123"));
            accessTokens.add(registerJson.get("accessToken").asText());
        }

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            me(accessTokens.get(i));
        }

        for (int i = WARMUP_ITERATIONS; i < accessTokens.size(); i++) {
            long start = System.nanoTime();
            me(accessTokens.get(i));
            values.add(toMs(System.nanoTime() - start));
        }

        return computeStats(values);
    }

    private LatencyStats computeStats(List<Long> values) {
        List<Long> sorted = values.stream().sorted(Comparator.naturalOrder()).toList();

        long average = (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        int p95Index = (int) Math.ceil(sorted.size() * 0.95) - 1;
        long p95 = sorted.get(Math.max(0, p95Index));

        return new LatencyStats(average, p95);
    }

    private long toMs(long nanos) {
        return nanos / 1_000_000;
    }

    private String register(String email, String password) throws Exception {
        MvcResult result = mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String refresh(String refreshToken) throws Exception {
        MvcResult result = mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private void validate(String accessToken) throws Exception {
        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private void me(String accessToken) throws Exception {
        mockMvc().perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.com";
    }

    private String registerPayload(String email, String password) {
        return "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";
    }

    private String loginPayload(String email, String password) {
        return "{" +
                "\"email\":\"" + email + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";
    }

    private String refreshPayload(String refreshToken) {
        return "{" +
                "\"refreshToken\":\"" + refreshToken + "\"" +
                "}";
    }

    private record LatencyStats(long averageMs, long p95Ms) {}
}
