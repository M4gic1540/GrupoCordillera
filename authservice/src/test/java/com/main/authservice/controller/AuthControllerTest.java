package com.main.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.authservice.dto.AuthResponse;
import com.main.authservice.dto.LoginRequest;
import com.main.authservice.dto.RefreshRequest;
import com.main.authservice.dto.RegisterRequest;
import com.main.authservice.exception.ApiExceptionHandler;
import com.main.authservice.model.Role;
import com.main.authservice.security.JwtService;
import com.main.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private final AuthService authService = org.mockito.Mockito.mock(AuthService.class);
    private final JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

    private MockMvc mockMvc() {
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new AuthController(authService, jwtService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldReturnCreatedWithBody() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password123");

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse(1L, "user@test.com"));

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void registerShouldReturnBadRequestWhenEmailMissing() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("Password123");

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerShouldReturnBadRequestWhenPasswordWeak() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("weakpass");

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void loginShouldReturnOkWithBody() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password123");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse(2L, "user@test.com"));

        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void loginShouldReturnBadRequestWhenInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("Password123");

        mockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void refreshShouldReturnOkWithBody() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(authResponse(3L, "ref@test.com"));

        mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ref@test.com"));
    }

    @Test
    void refreshShouldReturnBadRequestWhenTokenBlank() throws Exception {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("   ");

        mockMvc().perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void validateShouldReturnUnauthorizedWhenHeaderMissing() throws Exception {
        mockMvc().perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateShouldReturnUnauthorizedWhenHeaderWithoutBearerPrefix() throws Exception {
        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Token abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateShouldReturnOkWhenTokenIsValid() throws Exception {
        when(jwtService.isTokenValid(eq("valid-token"))).thenReturn(true);

        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void validateShouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
        when(jwtService.isTokenValid(eq("invalid-token"))).thenReturn(false);

        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateShouldReturnUnauthorizedWhenJwtServiceThrows() throws Exception {
        doThrow(new RuntimeException("bad token"))
                .when(jwtService)
                .isTokenValid(eq("boom-token"));

        mockMvc().perform(get("/api/auth/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer boom-token"))
                .andExpect(status().isUnauthorized());
    }

    private AuthResponse authResponse(Long id, String email) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("access-token");
        response.setRefreshToken("refresh-token");
        response.setTokenType("Bearer");
        response.setUserId(id);
        response.setEmail(email);
        response.setRole(Role.USER);
        return response;
    }
}
