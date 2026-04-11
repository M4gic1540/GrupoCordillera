package com.main.gateway.security;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final GatewaySecurityProperties properties;
    private final WebClient gatewayWebClient;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AuthenticationFilter(GatewaySecurityProperties properties, WebClient gatewayWebClient) {
        this.properties = properties;
        this.gatewayWebClient = gatewayWebClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String requestPath = exchange.getRequest().getPath().value();

        if (!requiresAuthentication(requestPath)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return gatewayWebClient.get()
                .uri(properties.getAuthValidationUrl())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return chain.filter(exchange);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean requiresAuthentication(String requestPath) {
        if (matchesAny(requestPath, properties.getExcludedPaths())) {
            return false;
        }
        return matchesAny(requestPath, properties.getProtectedPaths());
    }

    private boolean matchesAny(String path, List<String> patterns) {
        for (String pattern : patterns) {
            if (antPathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
