package com.main.dataingestion.repository;

import com.main.dataingestion.domain.IngestionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionEventRepository extends JpaRepository<IngestionEvent, Long> {
}
