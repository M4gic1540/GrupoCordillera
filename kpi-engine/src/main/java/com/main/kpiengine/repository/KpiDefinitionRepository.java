package com.main.kpiengine.repository;

import com.main.kpiengine.domain.KpiDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, Long> {

    Optional<KpiDefinition> findByCode(String code);
}
