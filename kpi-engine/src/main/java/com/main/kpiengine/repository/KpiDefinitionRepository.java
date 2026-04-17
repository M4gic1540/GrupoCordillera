package com.main.kpiengine.repository;

import com.main.kpiengine.domain.KpiDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a definiciones de KPI.
 *
 * <p>Permite CRUD completo por herencia de {@link JpaRepository} y consultas
 * derivadas por código funcional de KPI.</p>
 */
public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, Long> {

    /**
     * Busca una definición KPI por su código único de negocio.
     *
     * @param code código del KPI (ej: INGEST_THROUGHPUT).
     * @return definición encontrada o vacío si no existe.
     */
    Optional<KpiDefinition> findByCode(String code);
}
