package com.main.authservice.external;

/*
 * ExternalIntegrationExample - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy
 */


public class ExternalIntegrationExample {
    public static void main(String[] args) {
        ExternalConnector pos = ConnectorFactory.createConnector("POS");
        pos.connect();
        ExternalConnector erp = ConnectorFactory.createConnector("ERP");
        erp.connect();
    }
}
