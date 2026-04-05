package com.main.gateway.testutil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class GatewayHttpStubs {

    private final HttpServer authValidation;
    private final HttpServer authService1;
    private final HttpServer authService2;
    private final HttpServer ingestion1;
    private final HttpServer ingestion2;
    private final HttpServer kpi;

    private GatewayHttpStubs(
            HttpServer authValidation,
            HttpServer authService1,
            HttpServer authService2,
            HttpServer ingestion1,
            HttpServer ingestion2,
            HttpServer kpi
    ) {
        this.authValidation = authValidation;
        this.authService1 = authService1;
        this.authService2 = authService2;
        this.ingestion1 = ingestion1;
        this.ingestion2 = ingestion2;
        this.kpi = kpi;
    }

    public static GatewayHttpStubs start() {
        try {
            HttpServer authValidation = server(true, "auth-validation");
            HttpServer authService1 = server(false, "auth-service-1");
            HttpServer authService2 = server(false, "auth-service-2");
            HttpServer ingestion1 = server(false, "ingestion-1");
            HttpServer ingestion2 = server(false, "ingestion-2");
            HttpServer kpi = server(false, "kpi");

            return new GatewayHttpStubs(authValidation, authService1, authService2, ingestion1, ingestion2, kpi);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start HTTP stubs for gateway tests", e);
        }
    }

    public String authValidationUrl() {
        return "http://localhost:" + authValidation.getAddress().getPort() + "/api/auth/validate";
    }

    public String authService1Url() {
        return "http://localhost:" + authService1.getAddress().getPort();
    }

    public String authService2Url() {
        return "http://localhost:" + authService2.getAddress().getPort();
    }

    public String ingestion1Url() {
        return "http://localhost:" + ingestion1.getAddress().getPort();
    }

    public String ingestion2Url() {
        return "http://localhost:" + ingestion2.getAddress().getPort();
    }

    public String kpiUrl() {
        return "http://localhost:" + kpi.getAddress().getPort();
    }

    public void stop() {
        authValidation.stop(0);
        authService1.stop(0);
        authService2.stop(0);
        ingestion1.stop(0);
        ingestion2.stop(0);
        kpi.stop(0);
    }

    private static HttpServer server(boolean validateAuthHeader, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", new OkHandler(validateAuthHeader, body));
        server.setExecutor(null);
        server.start();
        return server;
    }

    private static final class OkHandler implements HttpHandler {
        private final boolean validateAuthHeader;
        private final String body;

        private OkHandler(boolean validateAuthHeader, String body) {
            this.validateAuthHeader = validateAuthHeader;
            this.body = body;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (validateAuthHeader) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || authHeader.isBlank() || authHeader.contains("invalid") || authHeader.contains("malicious")) {
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }
            }

            byte[] responseBytes = body.getBytes();
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }
    }
}
