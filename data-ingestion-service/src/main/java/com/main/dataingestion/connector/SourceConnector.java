package com.main.dataingestion.connector;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface SourceConnector {

    /**
     * Identificador lógico de la fuente que atiende este conector.
     */
    String sourceKey();

    /**
     * Recupera un lote de eventos desde la fuente externa.
     *
     * @return lista de payloads JSON normalizados para el pipeline de ingestión.
     */
    List<JsonNode> fetchBatch();
}
