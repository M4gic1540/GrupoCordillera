package com.main.authservice.external;

/*
 * ConnectorFactory - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy, Factory Method
 */


public class ConnectorFactory {
    /**
     * Crea un conector externo concreto segÃºn el tipo solicitado.
     *
     * @param type tipo de integraciÃ³n (por ejemplo POS o ERP).
     * @return implementaciÃ³n especÃ­fica de {@link ExternalConnector}.
     */
    public static ExternalConnector createConnector(String type) {
        return switch (type.toUpperCase()) {
            case "POS" -> new PosConnector();
            case "ERP" -> new ErpConnector();
            // Agrega mÃ¡s casos para otros conectores
            default -> throw new IllegalArgumentException("Tipo de conector desconocido: " + type);
        };
    }
}
