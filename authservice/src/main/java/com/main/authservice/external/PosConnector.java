package com.main.authservice.external;

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
