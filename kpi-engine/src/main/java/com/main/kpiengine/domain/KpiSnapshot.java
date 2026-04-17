package com.main.kpiengine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad de snapshot KPI calculado.
 *
 * <p>Representa el resultado puntual de un cálculo para una fuente específica,
 * enlazado a una definición KPI del catálogo.</p>
 */
@Entity
@Table(name = "kpi_snapshots")
public class KpiSnapshot {

    /** Identificador técnico autogenerado del snapshot. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Definición KPI asociada al valor calculado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KpiDefinition definition;

    /** Sistema origen que disparó el recálculo (ej: crm, erp). */
    @Column(nullable = false, length = 80)
    private String sourceSystem;

    /** Volumen de registros considerados durante el cálculo. */
    @Column(nullable = false)
    private Integer affectedRecords;

    /** Valor numérico del KPI con precisión monetaria/analítica. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    /** Fecha y hora exacta del cálculo del snapshot. */
    @Column(nullable = false)
    private OffsetDateTime computedAt;

    /** Si no se setea fecha explícita, usa el instante actual al persistir. */
    @PrePersist
    void onCreate() {
        if (this.computedAt == null) {
            this.computedAt = OffsetDateTime.now();
        }
    }

    /** @return id técnico del snapshot. */
    public Long getId() {
        return id;
    }

    /** @return definición KPI asociada. */
    public KpiDefinition getDefinition() {
        return definition;
    }

    /** @param definition definición KPI asociada al snapshot. */
    public void setDefinition(KpiDefinition definition) {
        this.definition = definition;
    }

    /** @return sistema fuente que originó el cálculo. */
    public String getSourceSystem() {
        return sourceSystem;
    }

    /** @param sourceSystem sistema fuente del evento calculado. */
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    /** @return cantidad de registros usados en el cálculo. */
    public Integer getAffectedRecords() {
        return affectedRecords;
    }

    /** @param affectedRecords volumen de entrada para el recálculo. */
    public void setAffectedRecords(Integer affectedRecords) {
        this.affectedRecords = affectedRecords;
    }

    /** @return valor final del KPI. */
    public BigDecimal getValue() {
        return value;
    }

    /** @param value valor calculado del KPI. */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /** @return timestamp de cómputo del snapshot. */
    public OffsetDateTime getComputedAt() {
        return computedAt;
    }

    /** @param computedAt fecha/hora explícita del cálculo. */
    public void setComputedAt(OffsetDateTime computedAt) {
        this.computedAt = computedAt;
    }
}
