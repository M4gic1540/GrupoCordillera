package com.main.authservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.authservice.dto.LoginRequest;
import com.main.authservice.dto.RefreshRequest;
import com.main.authservice.dto.RegisterRequest;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity(springSecurityFilterChain))
                .build();
    }

    @Test
    void registerShouldCreateUserAndReturnTokens() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("integration-register@test.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("integration-register@test.com"));
    }

    @Test
    void registerShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("integration-dup@test.com");
        request.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void loginShouldAuthenticateAndReturnTokens() throws Exception {
        registerUser("integration-login@test.com", "Password123");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("integration-login@test.com");
        loginRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("integration-login@test.com"));
    }

    @Test
    void loginShouldRejectInvalidCredentials() throws Exception {
        registerUser("integration-bad-login@test.com", "Password123");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("integration-bad-login@test.com");
        loginRequest.setPassword("WrongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void refreshShouldRotateRefreshToken() throws Exception {
        String registerResponse = registerUser("integration-refresh@test.com", "Password123");
        JsonNode json = objectMapper.readTree(registerResponse);
        String originalRefreshToken = json.get("refreshToken").asText();

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(originalRefreshToken);

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newRefreshToken = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        assertNotEquals(originalRefreshToken, newRefreshToken);
    }

    @Test
    void refreshShouldRejectInvalidToken() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void validateShouldAcceptValidJwtToken() throws Exception {
        String registerResponse = registerUser("integration-validate@test.com", "Password123");
        String token = objectMapper.readTree(registerResponse).get("accessToken").asText();
        assertNotNull(token);

        mockMvc.perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void validateShouldRejectMissingToken() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meShouldReturnAuthenticatedUserProfile() throws Exception {
        String registerResponse = registerUser("integration-me@test.com", "Password123");
        String token = objectMapper.readTree(registerResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration-me@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void meShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError());
    }

    private String registerUser(String email, String password) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);

        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
