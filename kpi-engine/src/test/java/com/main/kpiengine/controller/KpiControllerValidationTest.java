package com.main.kpiengine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.main.kpiengine.dto.RecalculateKpiRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class KpiControllerValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldFailForInvalidSourceSystemFormat() {
        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("CRM!");
        request.setAffectedRecords(10);

        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("invalid format"));
    }

    @Test
    void shouldPassForValidPayload() {
        RecalculateKpiRequest request = new RecalculateKpiRequest();
        request.setSourceSystem("crm");
        request.setAffectedRecords(10);

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
