package com.main.kpiengine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RecalculateKpiRequest {

    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9-]{1,39}$", message = "sourceSystem has invalid format")
    private String sourceSystem;

    @Min(value = 0, message = "affectedRecords must be >= 0")
    private int affectedRecords;

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public int getAffectedRecords() {
        return affectedRecords;
    }

    public void setAffectedRecords(int affectedRecords) {
        this.affectedRecords = affectedRecords;
    }
}
