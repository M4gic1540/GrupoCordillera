package com.main.dataingestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class IngestionPayloadIntegrityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void payloadSerializationShouldRemainStableForHashingEquivalentChecks() throws Exception {
        JsonNode payload = OBJECT_MAPPER.readTree("{\"id\":101,\"type\":\"created\",\"source\":\"crm\"}");
        String canonical = OBJECT_MAPPER.writeValueAsString(payload);

        assertEquals("{\"id\":101,\"type\":\"created\",\"source\":\"crm\"}", canonical);
    }
}
