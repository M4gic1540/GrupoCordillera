package com.main.kpiengine.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Entidad catálogo de KPI.
 *
 * <p>Define metadatos estables de un indicador (código, nombre y frecuencia)
 * que luego son referenciados por snapshots calculados.</p>
 */
@Entity
@Table(name = "kpi_definitions")
public class KpiDefinition {

    /** Identificador técnico autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único de negocio del KPI (ej: INGEST_THROUGHPUT). */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    /** Nombre legible para clientes/API/dashboard. */
    @Column(nullable = false, length = 120)
    private String name;

    /** Frecuencia de actualización semántica del KPI. */
    @Column(nullable = false, length = 20)
    private String frequency;

    /** Fecha de creación de la definición, asignada una sola vez. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Inicializa timestamp de creación antes de persistir por primera vez. */
    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    /** @return id técnico de la definición KPI. */
    public Long getId() {
        return id;
    }

    /** @return código funcional único del indicador. */
    public String getCode() {
        return code;
    }

    /** @param code código funcional único del indicador. */
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

    /** @param frequency frecuencia de recálculo/actualización. */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    /** @return fecha de creación en base de datos. */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
