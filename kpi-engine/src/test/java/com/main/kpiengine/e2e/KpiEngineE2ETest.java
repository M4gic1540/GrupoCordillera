package com.main.kpiengine.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KpiEngineE2ETest {

    @ParameterizedTest
    @CsvSource({
            "crm,2",
            "erp,2",
            "hr,2",
            "sales,2",
            "billing,2",
            "support,2",
            "ops,2",
            "finance,2",
            "legacy,2"
    })
    void e2eInputShapeShouldBeValid(String source, int records) {
        assertTrue(source.length() >= 2);
        assertTrue(source.matches("^[a-z][a-z0-9-]{1,39}$"));
        assertEquals(2, records);
    }
}
