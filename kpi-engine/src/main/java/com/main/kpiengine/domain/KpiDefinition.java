package com.main.kpiengine.domain;

/*
 * KpiDefinition - Domain Model.
 * Responsibilities: Entidad del dominio y mapeo de persistencia.
 * Patterns: Domain Model
 */


import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidad catÃ¡logo de KPI.
 *
 * <p>Define metadatos estables de un indicador (cÃ³digo, nombre y frecuencia)
 * que luego son referenciados por snapshots calculados.</p>
 */
@Entity
@Table(name = "kpi_definitions")
public class KpiDefinition {

    /** Identificador tÃ©cnico autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** CÃ³digo Ãºnico de negocio del KPI (ej: INGEST_THROUGHPUT). */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    /** Nombre legible para clientes/API/dashboard. */
    @Column(nullable = false, length = 120)
    private String name;

    /** Frecuencia de actualizaciÃ³n semÃ¡ntica del KPI. */
    @Column(nullable = false, length = 20)
    private String frequency;

    /** Fecha de creaciÃ³n de la definiciÃ³n, asignada una sola vez. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Inicializa timestamp de creaciÃ³n antes de persistir por primera vez. */
    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    /** @return id tÃ©cnico de la definiciÃ³n KPI. */
    public Long getId() {
        return id;
    }

    /** @return cÃ³digo funcional Ãºnico del indicador. */
    public String getCode() {
        return code;
    }

    /** @param code cÃ³digo funcional Ãºnico del indicador. */
    public void setCode(String code) {
        this.code = code;
    }

    /** @return nombre de negocio del indicador. */
    public String getName() {
        return name;
    }

    /** @param name nombre visible del indicador. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return frecuencia declarada del indicador. */
    public String getFrequency() {
        return frequency;
    }

    /** @param frequency frecuencia de recÃ¡lculo/actualizaciÃ³n. */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /** @return fecha de creaciÃ³n en base de datos. */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
