package com.main.authservice.external;

/*
 * ExternalConnector - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy
 */


public interface ExternalConnector {
    String getType();
    void connect();
    // Puedes agregar mÃ©todos como fetchData(), sendData(), etc.
}
