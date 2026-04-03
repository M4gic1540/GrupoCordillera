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

@Entity
@Table(name = "kpi_snapshots")
public class KpiSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_id", nullable = false)
    private KpiDefinition definition;

    @Column(nullable = false, length = 80)
    private String sourceSystem;

    @Column(nullable = false)
    private Integer affectedRecords;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(nullable = false)
    private OffsetDateTime computedAt;

    @PrePersist
    void onCreate() {
        if (this.computedAt == null) {
            this.computedAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public KpiDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(KpiDefinition definition) {
        this.definition = definition;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public Integer getAffectedRecords() {
        return affectedRecords;
    }

    public void setAffectedRecords(Integer affectedRecords) {
        this.affectedRecords = affectedRecords;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public OffsetDateTime getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(OffsetDateTime computedAt) {
        this.computedAt = computedAt;
    }
}
