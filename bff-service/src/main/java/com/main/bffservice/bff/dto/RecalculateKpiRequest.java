package com.main.bffservice.bff.dto;

/*
 * RecalculateKpiRequest - DTO.
 * Responsibilities: Contrato de datos para capa API.
 * Patterns: DTO, BFF
 */


public class RecalculateKpiRequest {
    private String sourceSystem;
    private Integer affectedRecords;

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public Integer getAffectedRecords() { return affectedRecords; }
    public void setAffectedRecords(Integer affectedRecords) { this.affectedRecords = affectedRecords; }
}
