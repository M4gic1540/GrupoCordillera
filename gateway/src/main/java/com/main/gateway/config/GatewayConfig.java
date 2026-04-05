package com.main.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.main.gateway.security.GatewaySecurityProperties;

@Configuration
@EnableConfigurationProperties(GatewaySecurityProperties.class)
public class GatewayConfig {

    @Bean
    public WebClient gatewayWebClient(WebClient.Builder builder) {
        return builder.build();
    }
}
