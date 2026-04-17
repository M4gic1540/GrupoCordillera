
// Paquete security: define propiedades de seguridad para gateway
package com.main.gateway.security;


import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


// Clase de propiedades de seguridad para gateway
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    // URL para validar JWT (endpoint en authservice)
    private String authValidationUrl;
    // Rutas protegidas: requieren autenticación
    private List<String> protectedPaths = new ArrayList<>();
    // Rutas excluidas: no requieren autenticación
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
