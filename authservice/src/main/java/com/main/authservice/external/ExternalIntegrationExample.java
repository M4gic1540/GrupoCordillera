package com.main.authservice.external;

public class ExternalIntegrationExample {
    public static void main(String[] args) {
        ExternalConnector pos = ConnectorFactory.createConnector("POS");
        pos.connect();
        ExternalConnector erp = ConnectorFactory.createConnector("ERP");
        erp.connect();
    }
}
