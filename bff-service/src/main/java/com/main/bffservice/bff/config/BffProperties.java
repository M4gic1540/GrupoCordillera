package com.main.bffservice.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bff")
public class BffProperties {

    private String authBaseUrl;
    private String ingestionBaseUrl;
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

