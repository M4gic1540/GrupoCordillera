package com.main.authservice.repository;

/*
 * RefreshTokenRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.authservice.model.RefreshToken;
import com.main.authservice.model.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    long deleteByUser(User user);

    long deleteByExpiresAtBefore(Instant cutoff);
}
