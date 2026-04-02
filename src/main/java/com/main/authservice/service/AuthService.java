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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);

        RefreshToken refreshToken = createAndSaveRefreshToken(savedUser);
        return buildAuthResponse(savedUser, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = createAndSaveRefreshToken(user);
        return buildAuthResponse(user, refreshToken.getToken());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);
            throw new UnauthorizedException("Refresh token expired");
        }

        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        User user = oldToken.getUser();
        RefreshToken newToken = createAndSaveRefreshToken(user);
        return buildAuthResponse(user, newToken.getToken());
    }

    @Transactional(readOnly = true)
    public UserMeResponse me(String email) {
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
