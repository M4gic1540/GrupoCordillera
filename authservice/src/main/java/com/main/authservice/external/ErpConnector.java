package com.main.authservice.external;

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
