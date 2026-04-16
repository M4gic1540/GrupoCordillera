package com.main.kpiengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para solicitar recálculo de KPIs.
 *
 * <p>Se valida antes de entrar a la capa de servicio para asegurar
 * consistencia del origen y del volumen reportado.</p>
 */
public class RecalculateKpiRequest {

    /** Clave del sistema fuente que disparó el recálculo (ej: crm, erp). */
    @NotBlank(message = "sourceSystem is required")
    @Size(min = 2, max = 40, message = "sourceSystem length must be between 2 and 40")
    @Pattern(regexp = "^[a-z][a-z0-9-]{1,39}$", message = "sourceSystem has invalid format")
    private String sourceSystem;

    /** Cantidad de registros afectados por la ingestión reciente. */
    @NotNull(message = "affectedRecords is required")
    @Min(value = 0, message = "affectedRecords must be >= 0")
    private Integer affectedRecords;

    /** @return sistema fuente asociado al evento de recálculo. */
    public String getSourceSystem() {
        return sourceSystem;
    }

    /** @param sourceSystem sistema fuente a persistir/procesar. */
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    /** @return cantidad de registros impactados. */
    public Integer getAffectedRecords() {
        return affectedRecords;
    }

    /** @param affectedRecords volumen afectado para el cálculo KPI. */
    public void setAffectedRecords(Integer affectedRecords) {
        this.affectedRecords = affectedRecords;
    }
}
