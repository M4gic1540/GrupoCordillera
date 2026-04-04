package com.main.authservice.service;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class AuthServiceSecurityTest {

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
    void registerShouldNormalizeEmailForSecurity() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  SECURE@TEST.COM ");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("secure@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository).existsByEmail("secure@test.com");
    }

    @Test
    void registerShouldBlockDuplicateIdentity() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dup@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldHashPasswordBeforeSave() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("hash@test.com");
        request.setPassword("PlainSecret123");

        when(userRepository.existsByEmail("hash@test.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainSecret123")).thenReturn("hashed-secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("hash@test.com")).thenReturn("jwt");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("hashed-secret", userCaptor.getValue().getPasswordHash());
        assertNotEquals("PlainSecret123", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void registerShouldForceUserRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("role-sec@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("role-sec@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("role-sec@test.com")).thenReturn("jwt");

        AuthResponse response = authService.register(request);

        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void registerShouldReturnBearerTokenType() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("bearer@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("bearer@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("bearer@test.com")).thenReturn("jwt");

        AuthResponse response = authService.register(request);

        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void registerShouldStoreNonRevokedRefreshToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("token-sec@test.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("token-sec@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(4L);
            return u;
        });
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("token-sec@test.com")).thenReturn("jwt");

        authService.register(request);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertFalse(tokenCaptor.getValue().isRevoked());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void loginShouldNormalizeEmailBeforeAuthentication() {
        LoginRequest request = new LoginRequest();
        request.setEmail("  LOGIN.SEC@TEST.COM ");
        request.setPassword("Password123");

        User user = user(10L, "login.sec@test.com", Role.USER);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("login.sec@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("login.sec@test.com")).thenReturn("jwt");

        authService.login(request);

        verify(userRepository).findByEmail("login.sec@test.com");
    }

    @Test
    void loginShouldRejectBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("bad");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldRejectWhenUserNotFoundAfterAuthentication() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@test.com");
        request.setPassword("Password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldInvalidatePreviousRefreshTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rotate@test.com");
        request.setPassword("Password123");

        User user = user(11L, "rotate@test.com", Role.USER);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("rotate@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("rotate@test.com")).thenReturn("jwt");

        authService.login(request);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void loginShouldIssueRefreshTokenWithStrongLength() {
        LoginRequest request = new LoginRequest();
        request.setEmail("entropy@test.com");
        request.setPassword("Password123");

        User user = user(12L, "entropy@test.com", Role.USER);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("entropy@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("entropy@test.com")).thenReturn("jwt");

        AuthResponse response = authService.login(request);

        assertNotNull(response.getRefreshToken());
        assertTrue(response.getRefreshToken().length() >= 80);
    }

    @Test
    void refreshShouldRejectUnknownToken() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("unknown");

        when(refreshTokenRepository.findByTokenAndRevokedFalse("unknown")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
    }

    @Test
    void refreshShouldRevokeExpiredTokenAndReject() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired");

        User user = user(20L, "expired@test.com", Role.USER);
        RefreshToken old = token("expired", user, Instant.now().minusSeconds(30), false);
        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired")).thenReturn(Optional.of(old));

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
        assertTrue(old.isRevoked());
        verify(refreshTokenRepository).save(old);
    }

    @Test
    void refreshShouldRotateTokenAndRevokeOldOne() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid");

        User user = user(21L, "rotate2@test.com", Role.ADMIN);
        RefreshToken old = token("valid", user, Instant.now().plusSeconds(120), false);
        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid")).thenReturn(Optional.of(old));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("rotate2@test.com")).thenReturn("jwt");

        AuthResponse response = authService.refresh(request);

        assertTrue(old.isRevoked());
        assertNotNull(response.getRefreshToken());
        assertNotEquals("valid", response.getRefreshToken());
    }

    @Test
    void refreshShouldKeepSameUserIdentityAfterRotation() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-identity");

        User user = user(22L, "identity@test.com", Role.USER);
        RefreshToken old = token("valid-identity", user, Instant.now().plusSeconds(120), false);
        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-identity")).thenReturn(Optional.of(old));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("identity@test.com")).thenReturn("jwt");

        AuthResponse response = authService.refresh(request);

        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getId(), response.getUserId());
    }

    @Test
    void refreshShouldGenerateAccessTokenForTokenOwnerEmail() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-owner");

        User user = user(23L, "owner.security@test.com", Role.USER);
        RefreshToken old = token("valid-owner", user, Instant.now().plusSeconds(120), false);
        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-owner")).thenReturn(Optional.of(old));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("owner.security@test.com")).thenReturn("jwt");

        authService.refresh(request);

        verify(jwtService).generateAccessToken("owner.security@test.com");
    }

    @Test
    void meShouldRejectUnknownUser() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.me("none@test.com"));
    }

    @Test
    void meShouldReturnOnlyAuthenticatedUserProfile() {
        User user = user(30L, "me.security@test.com", Role.ADMIN);
        user.setCreatedAt(Instant.now().minusSeconds(100));
        when(userRepository.findByEmail("me.security@test.com")).thenReturn(Optional.of(user));

        UserMeResponse response = authService.me("me.security@test.com");

        assertEquals(30L, response.getId());
        assertEquals("me.security@test.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void loginShouldGenerateDifferentRefreshTokensAcrossSessions() {
        LoginRequest request = new LoginRequest();
        request.setEmail("session@test.com");
        request.setPassword("Password123");

        User user = user(40L, "session@test.com", Role.USER);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByEmail("session@test.com")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("session@test.com")).thenReturn("jwt");

        AuthResponse first = authService.login(request);
        AuthResponse second = authService.login(request);

        assertNotEquals(first.getRefreshToken(), second.getRefreshToken());
    }

    @Test
    void refreshShouldRejectReusingAlreadyRevokedToken() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("one-time-token");

        User user = user(50L, "reuse@test.com", Role.USER);
        RefreshToken old = token("one-time-token", user, Instant.now().plusSeconds(120), false);

        when(refreshTokenRepository.findByTokenAndRevokedFalse("one-time-token"))
                .thenReturn(Optional.of(old))
                .thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken("reuse@test.com")).thenReturn("jwt");

        AuthResponse first = authService.refresh(request);
        assertNotNull(first.getRefreshToken());

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
    }

    private User user(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private RefreshToken token(String value, User user, Instant expiresAt, boolean revoked) {
        RefreshToken token = new RefreshToken();
        token.setToken(value);
        token.setUser(user);
        token.setExpiresAt(expiresAt);
        token.setRevoked(revoked);
        return token;
    }
}
