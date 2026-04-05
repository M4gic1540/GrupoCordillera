package com.main.authservice.external;

public class ConnectorFactory {
    public static ExternalConnector createConnector(String type) {
        return switch (type.toUpperCase()) {
            case "POS" -> new PosConnector();
            case "ERP" -> new ErpConnector();
            // Agrega más casos para otros conectores
            default -> throw new IllegalArgumentException("Tipo de conector desconocido: " + type);
        };
    }
}
