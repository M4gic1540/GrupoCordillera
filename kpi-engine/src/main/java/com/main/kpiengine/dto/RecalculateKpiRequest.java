package com.main.kpiengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RecalculateKpiRequest {

    @NotBlank(message = "sourceSystem is required")
    @Size(min = 2, max = 40, message = "sourceSystem length must be between 2 and 40")
    @Pattern(regexp = "^[a-z][a-z0-9-]{1,39}$", message = "sourceSystem has invalid format")
    private String sourceSystem;

    @NotNull(message = "affectedRecords is required")
    @Min(value = 0, message = "affectedRecords must be >= 0")
    private Integer affectedRecords;

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
}
