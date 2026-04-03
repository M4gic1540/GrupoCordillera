package com.main.authservice.service;

import com.main.authservice.dto.RegisterRequest;
import com.main.authservice.model.User;
import com.main.authservice.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServicePasswordHashingTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerStoresPasswordAsHash() {
        String rawPassword = "Password123";
        String email = "user-" + UUID.randomUUID() + "@example.com";

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(rawPassword);

        authService.register(request);

        User storedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User was not stored"));

        assertNotEquals(rawPassword, storedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, storedUser.getPasswordHash()));
    }
}
