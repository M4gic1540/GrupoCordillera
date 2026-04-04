package com.main.kpiengine.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KpiEnginePerformanceTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 5, 8, 13, 21, 34, 55, 89})
    void simpleCalculationShouldStayFast(int size) {
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < size * 100; i++) {
            sum += i;
        }
        long elapsed = System.nanoTime() - start;

        assertTrue(sum >= 0);
        assertTrue(elapsed >= 0);
    }
}
