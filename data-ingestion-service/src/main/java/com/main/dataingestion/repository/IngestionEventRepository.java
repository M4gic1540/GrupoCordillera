package com.main.dataingestion.repository;

/*
 * IngestionEventRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.dataingestion.domain.IngestionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionEventRepository extends JpaRepository<IngestionEvent, Long> {
}
