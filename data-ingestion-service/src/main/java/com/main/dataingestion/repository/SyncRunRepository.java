package com.main.dataingestion.repository;

/*
 * SyncRunRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.dataingestion.domain.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {
}
