package com.main.dataingestion.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.main.dataingestion.service.IngestionService;
import com.main.dataingestion.service.IngestionService.SyncResult;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {IngestionController.class, ApiExceptionHandler.class})
@Import(IngestionControllerValidationTest.TestBeans.class)
class IngestionControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestBeans {
        @Bean
        IngestionService ingestionService() {
            return new IngestionService(null, null, null, null) {
                @Override
                public SyncResult ingest(String sourceSystem) {
                    return new SyncResult(sourceSystem, 2, OffsetDateTime.now());
                }
            };
        }
    }

    @Test
    void shouldReturnBadRequestWhenSourceSystemFormatIsInvalid() throws Exception {
        mockMvc.perform(post("/api/ingestion/sync/CRM!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkWhenSourceSystemFormatIsValid() throws Exception {
        mockMvc.perform(post("/api/ingestion/sync/crm"))
                .andExpect(status().isOk());
    }
}
