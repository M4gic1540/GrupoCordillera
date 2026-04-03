package com.main.dataingestion.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "source-systems")
public class SourceSystemsProperties {

    @NotEmpty
    private List<SourceSystemConfig> systems;

    public List<SourceSystemConfig> getSystems() {
        return systems;
    }

    public void setSystems(List<SourceSystemConfig> systems) {
        this.systems = systems;
    }

    public static class SourceSystemConfig {
        @NotBlank
        private String key;

        @NotBlank
        private String baseUrl;

        @NotNull
        private Integer timeoutMs;

        @NotBlank
        private String endpointPath;

        @NotBlank
        private String circuitBreakerName;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Integer getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(Integer timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public String getEndpointPath() {
            return endpointPath;
        }

        public void setEndpointPath(String endpointPath) {
            this.endpointPath = endpointPath;
        }

        public String getCircuitBreakerName() {
            return circuitBreakerName;
        }

        public void setCircuitBreakerName(String circuitBreakerName) {
            this.circuitBreakerName = circuitBreakerName;
        }
    }
}
