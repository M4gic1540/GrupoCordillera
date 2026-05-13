package com.main.kpiengine.repository;

/*
 * KpiDefinitionRepository - Repository.
 * Responsibilities: Acceso a datos mediante Spring Data JPA.
 * Patterns: Repository
 */


import com.main.kpiengine.domain.KpiDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de acceso a definiciones de KPI.
 *
 * <p>Permite CRUD completo por herencia de {@link JpaRepository} y consultas
 * derivadas por cÃ³digo funcional de KPI.</p>
 */
public interface KpiDefinitionRepository extends JpaRepository<KpiDefinition, Long> {

    /**
     * Busca una definiciÃ³n KPI por su cÃ³digo Ãºnico de negocio.
     *
     * @param code cÃ³digo del KPI (ej: INGEST_THROUGHPUT).
     * @return definiciÃ³n encontrada o vacÃ­o si no existe.
     */
    Optional<KpiDefinition> findByCode(String code);
}
