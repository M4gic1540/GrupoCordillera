package com.main.dataingestion.connector;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public interface SourceConnector {

    String sourceKey();

    List<JsonNode> fetchBatch();
}
