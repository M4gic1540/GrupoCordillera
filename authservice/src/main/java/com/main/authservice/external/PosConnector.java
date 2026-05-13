package com.main.authservice.external;

/*
 * PosConnector - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy
 */


public class PosConnector implements ExternalConnector {
    @Override
    public String getType() {
        return "POS";
    }

    @Override
    public void connect() {
        System.out.println("Conectando a POS...");
    }
}
