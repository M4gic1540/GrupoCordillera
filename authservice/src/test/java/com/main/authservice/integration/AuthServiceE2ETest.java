package com.main.authservice.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("local")
class AuthServiceE2ETest {

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
    void e2eRegisterShouldReturnCreatedWithTokens() throws Exception {
        String email = uniqueEmail("e2e-register");

        String body = register(email, "Password123").getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("accessToken").asText());
        assertNotNull(json.get("refreshToken").asText());
        assertEquals(email, json.get("email").asText());
    }

    @Test
    void e2eRegisterShouldRejectDuplicateEmail() {
        String email = uniqueEmail("e2e-duplicate");

        assertNotNull(email);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            register(email, "Password123");
            mockMvc().perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerPayload(email, "Password123")))
                    .andExpect(status().isConflict());
        });
    }

    @Test
    void e2eLoginShouldReturnOkWithTokens() throws Exception {
        String email = uniqueEmail("e2e-login");
        register(email, "Password123");

        String body = login(email, "Password123").getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        assertNotNull(json.get("accessToken").asText());
        assertNotNull(json.get("refreshToken").asText());
        assertEquals(email, json.get("email").asText());
    }

    @Test
    void e2eLoginShouldRejectWrongPassword() {
        String email = uniqueEmail("e2e-login-bad-pass");
        assertNotNull(email);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            register(email, "Password123");
            mockMvc().perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload(email, "WrongPassword123")))
                    .andExpect(status().isUnauthorized());
        });
    }

    @Test
    void e2eRefreshShouldRotateToken() throws Exception {
        String email = uniqueEmail("e2e-refresh");
        String registerBody = register(email, "Password123").getResponse().getContentAsString();
        String originalRefresh = objectMapper.readTree(registerBody).get("refreshToken").asText();

        String refreshBody = refresh(originalRefresh).getResponse().getContentAsString();
        JsonNode refreshedJson = objectMapper.readTree(refreshBody);
        String newRefresh = refreshedJson.get("refreshToken").asText();

        assertNotNull(refreshedJson.get("accessToken").asText());
        assertNotEquals(originalRefresh, newRefresh);
    }

    @Test
    void e2eRefreshShouldRejectRevokedToken() throws Exception {
        String email = uniqueEmail("e2e-refresh-revoked");
        String registerBody = register(email, "Password123").getResponse().getContentAsString();
        String originalRefresh = objectMapper.readTree(registerBody).get("refreshToken").asText();

        refresh(originalRefresh);

        mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(originalRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void e2eValidateShouldReturnOkForValidAccessToken() throws Exception {
        String email = uniqueEmail("e2e-validate-ok");
        String registerBody = register(email, "Password123").getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registerBody).get("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION)))
                .andExpect(status().isOk());
    }

    @Test
    void e2eValidateShouldReturnUnauthorizedWithoutToken() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                mockMvc().perform(get("/api/auth/validate"))
                        .andExpect(status().isUnauthorized())
        );
    }

    @Test
    void e2eMeShouldReturnUserDataWhenAuthenticated() throws Exception {
        String email = uniqueEmail("e2e-me");
        String registerBody = register(email, "Password123").getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registerBody).get("accessToken").asText();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        MvcResult result = mockMvc().perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(email, json.get("email").asText());
        assertEquals("USER", json.get("role").asText());
    }

    @Test
    void e2eMeShouldReturnUnauthorizedWithoutToken() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                mockMvc().perform(get("/api/users/me"))
                        .andExpect(status().isForbidden())
        );
    }

    private MvcResult register(String email, String password) throws Exception {
        return mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(email, password)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MvcResult login(String email, String password) throws Exception {
        return mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, password)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private MvcResult refresh(String refreshToken) throws Exception {
        return mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();
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
}
