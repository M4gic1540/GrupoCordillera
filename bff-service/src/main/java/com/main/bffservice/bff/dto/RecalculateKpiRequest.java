package com.main.bffservice.bff.dto;

public class RecalculateKpiRequest {
    private String sourceSystem;
    private Integer affectedRecords;

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public Integer getAffectedRecords() { return affectedRecords; }
    public void setAffectedRecords(Integer affectedRecords) { this.affectedRecords = affectedRecords; }
}
