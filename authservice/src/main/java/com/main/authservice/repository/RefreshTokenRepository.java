package com.main.authservice.repository;

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
