package com.main.authservice.external;

public class ConnectorFactory {
    /**
     * Crea un conector externo concreto según el tipo solicitado.
     *
     * @param type tipo de integración (por ejemplo POS o ERP).
     * @return implementación específica de {@link ExternalConnector}.
     */
    public static ExternalConnector createConnector(String type) {
        return switch (type.toUpperCase()) {
            case "POS" -> new PosConnector();
            case "ERP" -> new ErpConnector();
            // Agrega más casos para otros conectores
            default -> throw new IllegalArgumentException("Tipo de conector desconocido: " + type);
        };
    }
}
