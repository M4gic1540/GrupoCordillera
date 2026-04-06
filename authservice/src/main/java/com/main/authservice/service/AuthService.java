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
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio central de autenticacion.
 *
 * <p>Orquesta registro, login, refresh token y consulta de perfil del usuario autenticado,
 * delegando persistencia en repositorios y emision de access token en {@link JwtService}.</p>
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @org.springframework.beans.factory.annotation.Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpirationMillis
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    /**
     * Registra un nuevo usuario y retorna el par de tokens inicial.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        logger.info("Register flow started for email={}", email);
        if (userRepository.existsByEmail(email)) {
            logger.warn("Register rejected, email already exists: {}", email);
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);
        logger.info("User created with id={} email={}", savedUser.getId(), savedUser.getEmail());

        RefreshToken refreshToken = createAndSaveRefreshToken(savedUser);
        return buildAuthResponse(savedUser, refreshToken.getToken());
    }

    /**
     * Autentica credenciales, revoca refresh tokens previos y emite un nuevo par de tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        logger.info("Login flow started for email={}", email);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException ex) {
            logger.warn("Login failed due to invalid credentials for email={}", email);
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = createAndSaveRefreshToken(user);
        logger.info("Login successful for userId={}", user.getId());
        return buildAuthResponse(user, refreshToken.getToken());
    }

    /**
     * Rota refresh token: invalida el token recibido y crea uno nuevo para el mismo usuario.
     */
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        logger.info("Refresh token flow started");
        RefreshToken oldToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);
            logger.warn("Refresh token rejected, token expired for userId={}", oldToken.getUser().getId());
            throw new UnauthorizedException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        User user = oldToken.getUser();
        RefreshToken newToken = createAndSaveRefreshToken(user);
        logger.info("Refresh token successful for userId={}", user.getId());
        return buildAuthResponse(user, newToken.getToken());
    }

    /**
     * Devuelve el perfil basico del usuario autenticado identificado por email.
     */
    @Transactional(readOnly = true)
    public UserMeResponse me(String email) {
        logger.debug("Fetching profile for email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        UserMeResponse response = new UserMeResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(jwtService.generateAccessToken(user.getEmail()));
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    private RefreshToken createAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateRefreshTokenValue());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMillis));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
