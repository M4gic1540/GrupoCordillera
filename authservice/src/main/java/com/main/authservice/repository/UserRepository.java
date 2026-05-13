package com.main.authservice.repository;

/*
 * UserRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.authservice.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(com.main.authservice.model.Role role);
}
