package com.main.authservice.external;

/*
 * ErpConnector - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy
 */


public class ErpConnector implements ExternalConnector {
    @Override
    public String getType() {
        return "ERP";
    }

    @Override
    public void connect() {
        System.out.println("Conectando a ERP...");
    }
}
