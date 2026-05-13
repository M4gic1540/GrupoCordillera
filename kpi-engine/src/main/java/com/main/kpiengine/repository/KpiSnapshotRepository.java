package com.main.kpiengine.repository;

/*
 * KpiSnapshotRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.kpiengine.domain.KpiSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para snapshots calculados de KPI.
 *
 * <p>Expone operaciones CRUD y consulta de Ãºltimos resultados para consumo
 * en API/BFF.</p>
 */
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshot, Long> {

    /**
     * Obtiene los 20 snapshots mÃ¡s recientes ordenados por fecha de cÃ¡lculo.
     *
     * @return lista de snapshots desde el mÃ¡s nuevo al mÃ¡s antiguo.
     */
    List<KpiSnapshot> findTop20ByOrderByComputedAtDesc();
}
