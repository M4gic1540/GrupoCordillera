package com.main.kpiengine.domain;

/*
 * KpiSnapshot - Domain Model.
 * Responsibilities: Entidad del dominio y mapeo de persistencia.
 * Patterns: Domain Model
 */


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
 * <p>Representa el resultado puntual de un cÃ¡lculo para una fuente especÃ­fica,
 * enlazado a una definiciÃ³n KPI del catÃ¡logo.</p>
 */
@Entity
@Table(name = "kpi_snapshots")
public class KpiSnapshot {

    /** Identificador tÃ©cnico autogenerado del snapshot. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DefiniciÃ³n KPI asociada al valor calculado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KpiDefinition definition;

    /** Sistema origen que disparÃ³ el recÃ¡lculo (ej: crm, erp). */
    @Column(nullable = false, length = 80)
    private String sourceSystem;

    /** Volumen de registros considerados durante el cÃ¡lculo. */
    @Column(nullable = false)
    private Integer affectedRecords;

    /** Valor numÃ©rico del KPI con precisiÃ³n monetaria/analÃ­tica. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    /** Fecha y hora exacta del cÃ¡lculo del snapshot. */
    @Column(nullable = false)
    private OffsetDateTime computedAt;

    /** Si no se setea fecha explÃ­cita, usa el instante actual al persistir. */
    @PrePersist
    void onCreate() {
        if (this.computedAt == null) {
            this.computedAt = OffsetDateTime.now();
        }
    }

    /** @return id tÃ©cnico del snapshot. */
    public Long getId() {
        return id;
    }

    /** @return definiciÃ³n KPI asociada. */
    public KpiDefinition getDefinition() {
        return definition;
    }

    /** @param definition definiciÃ³n KPI asociada al snapshot. */
    public void setDefinition(KpiDefinition definition) {
        this.definition = definition;
    }

    /** @return sistema fuente que originÃ³ el cÃ¡lculo. */
    public String getSourceSystem() {
        return sourceSystem;
    }

    /** @param sourceSystem sistema fuente del evento calculado. */
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    /** @return cantidad de registros usados en el cÃ¡lculo. */
    public Integer getAffectedRecords() {
        return affectedRecords;
    }

    /** @param affectedRecords volumen de entrada para el recÃ¡lculo. */
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

    /** @return timestamp de cÃ³mputo del snapshot. */
    public OffsetDateTime getComputedAt() {
        return computedAt;
    }

    /** @param computedAt fecha/hora explÃ­cita del cÃ¡lculo. */
    public void setComputedAt(OffsetDateTime computedAt) {
        this.computedAt = computedAt;
    }
}
