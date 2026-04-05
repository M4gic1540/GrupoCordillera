package com.main.authservice.external;

public interface ExternalConnector {
    String getType();
    void connect();
    // Puedes agregar métodos como fetchData(), sendData(), etc.
}
