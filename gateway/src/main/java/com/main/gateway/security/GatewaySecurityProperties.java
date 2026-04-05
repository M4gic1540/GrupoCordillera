package com.main.gateway.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    private String authValidationUrl;
    private List<String> protectedPaths = new ArrayList<>();
    private List<String> excludedPaths = new ArrayList<>();

    public String getAuthValidationUrl() {
        return authValidationUrl;
    }

    public void setAuthValidationUrl(String authValidationUrl) {
        this.authValidationUrl = authValidationUrl;
    }

    public List<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<String> protectedPaths) {
        this.protectedPaths = protectedPaths;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }
}
