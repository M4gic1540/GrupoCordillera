
// Paquete config: define propiedades de configuración para BFF
package com.main.gateway.bff.config;


// Permite mapear propiedades de config (application.yml) con prefijo bff
import org.springframework.boot.context.properties.ConfigurationProperties;


// Clase de propiedades para capa BFF
@ConfigurationProperties(prefix = "bff")
public class BffProperties {

    // URL base de authservice
    private String authBaseUrl;
    // URL base de data-ingestion-service
    private String ingestionBaseUrl;
    // URL base de kpi-engine
    private String kpiBaseUrl;

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public String getIngestionBaseUrl() {
        return ingestionBaseUrl;
    }

    public void setIngestionBaseUrl(String ingestionBaseUrl) {
        this.ingestionBaseUrl = ingestionBaseUrl;
    }

    public String getKpiBaseUrl() {
        return kpiBaseUrl;
    }

    public void setKpiBaseUrl(String kpiBaseUrl) {
        this.kpiBaseUrl = kpiBaseUrl;
    }
}
