package com.main.dataingestion.connector;

/*
 * SourceConnector - Integration.
 * Responsibilities: Abstraccion/implementacion de conectores externos.
 * Patterns: Strategy
 */


import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface SourceConnector {

    /**
     * Identificador lÃ³gico de la fuente que atiende este conector.
     */
    String sourceKey();

    /**
     * Recupera un lote de eventos desde la fuente externa.
     *
     * @return lista de payloads JSON normalizados para el pipeline de ingestiÃ³n.
     */
    List<JsonNode> fetchBatch();
}
