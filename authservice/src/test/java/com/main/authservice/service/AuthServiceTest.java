package com.main.authservice.service;

import com.main.authservice.dto.AuthResponse;
import com.main.authservice.dto.LoginRequest;
import com.main.authservice.dto.RefreshRequest;
import com.main.authservice.dto.RegisterRequest;
import com.main.authservice.dto.UserMeResponse;
import com.main.authservice.exception.ConflictException;
import com.main.authservice.exception.UnauthorizedException;
import com.main.authservice.model.RefreshToken;
import com.main.authservice.model.Role;
import com.main.authservice.model.User;
import com.main.authservice.repository.RefreshTokenRepository;
import com.main.authservice.repository.UserRepository;
import com.main.authservice.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                86_400_000L
        );
    }

    @Test
    void registerShouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(" User@Test.com ");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken token = inv.getArgument(0);
            token.setToken("refresh-token");
            return token;
        });
        when(jwtService.generateAccessToken("user@test.com")).thenReturn("access-token");

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("user@test.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("user@test.com", userCaptor.getValue().getEmail());
        assertEquals("hashed-pwd", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void registerShouldThrowConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldThrowUnauthorizedWhenAuthenticationFails() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("bad");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldThrowUnauthorizedWhenUserNotFoundAfterAuth() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("Password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldRotateRefreshTokenAndReturnResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" USER@Test.com ");
        request.setPassword("Password123");

        User user = new User();
        user.setId(20L);
        user.setEmail("user@test.com");
        user.setRole(Role.USER);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken token = inv.getArgument(0);
            token.setToken("new-refresh");
            return token;
        });
        when(jwtService.generateAccessToken("user@test.com")).thenReturn("new-access");

        AuthResponse response = authService.login(request);

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void refreshShouldThrowUnauthorizedWhenTokenMissing() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("missing-token");

        when(refreshTokenRepository.findByTokenAndRevokedFalse("missing-token")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
    }

    @Test
    void refreshShouldRevokeExpiredTokenAndThrowUnauthorized() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired-token");

        User user = new User();
        user.setId(30L);
        user.setEmail("user@test.com");

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("expired-token");
        oldToken.setUser(user);
        oldToken.setExpiresAt(Instant.now().minusSeconds(60));
        oldToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token")).thenReturn(Optional.of(oldToken));

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
        assertTrue(oldToken.isRevoked());
        verify(refreshTokenRepository).save(oldToken);
    }

    @Test
    void refreshShouldRevokeOldTokenAndIssueNewToken() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-token");

        User user = new User();
        user.setId(40L);
        user.setEmail("user@test.com");
        user.setRole(Role.ADMIN);

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("valid-token");
        oldToken.setUser(user);
        oldToken.setExpiresAt(Instant.now().plusSeconds(300));
        oldToken.setRevoked(false);

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-token")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken token = inv.getArgument(0);
            if (!"valid-token".equals(token.getToken())) {
                token.setToken("rotated-token");
            }
            return token;
        });
        when(jwtService.generateAccessToken("user@test.com")).thenReturn("rotated-access");

        AuthResponse response = authService.refresh(request);

        assertTrue(oldToken.isRevoked());
        assertEquals("rotated-token", response.getRefreshToken());
        assertEquals("rotated-access", response.getAccessToken());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void meShouldReturnUserProfileWhenUserExists() {
        User user = new User();
        user.setId(50L);
        user.setEmail("me@test.com");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());

        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(user));

        UserMeResponse response = authService.me("me@test.com");

        assertEquals(50L, response.getId());
        assertEquals("me@test.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void meShouldThrowUnauthorizedWhenUserDoesNotExist() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.me("ghost@test.com"));
    }

    @Test
    void registerShouldSetRefreshTokenAsNotRevoked() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(60L);
            return u;
        });
        when(jwtService.generateAccessToken(any(String.class))).thenReturn("access");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertFalse(tokenCaptor.getValue().isRevoked());
        assertNotNull(tokenCaptor.getValue().getExpiresAt());
    }
}
