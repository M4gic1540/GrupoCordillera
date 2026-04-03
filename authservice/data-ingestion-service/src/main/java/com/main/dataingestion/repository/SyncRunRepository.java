package com.main.dataingestion.repository;

import com.main.dataingestion.domain.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {
}
