package com.main.kpiengine.repository;

import com.main.kpiengine.domain.KpiSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    List<KpiSnapshot> findTop20ByOrderByComputedAtDesc();
}
