package com.main.dataingestion.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KpiNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(KpiNotificationService.class);

    private final WebClient webClient;
    private final String recalcPath;

    public KpiNotificationService(WebClient.Builder webClientBuilder,
                                  @Value("${kpi-engine.base-url}") String baseUrl,
                                  @Value("${kpi-engine.recalc-path}") String recalcPath) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.recalcPath = recalcPath;
    }

    public void notifyRecalculation(String sourceSystem, int affectedRecords) {
        try {
            webClient.post()
                    .uri(recalcPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("sourceSystem", sourceSystem, "affectedRecords", affectedRecords))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            logger.info("KPI Engine notified for source {} with {} records", sourceSystem, affectedRecords);
        } catch (Exception ex) {
            logger.warn("KPI notification failed for source {}", sourceSystem);
        }
    }
}
