package com.main.kpiengine.repository;

import com.main.kpiengine.domain.KpiSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para snapshots calculados de KPI.
 *
 * <p>Expone operaciones CRUD y consulta de últimos resultados para consumo
 * en API/BFF.</p>
 */
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    /**
     * Obtiene los 20 snapshots más recientes ordenados por fecha de cálculo.
     *
     * @return lista de snapshots desde el más nuevo al más antiguo.
     */
    List<KpiSnapshot> findTop20ByOrderByComputedAtDesc();
}
