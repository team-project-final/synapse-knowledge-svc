package com.synapse.knowledge.graph.dto;

import java.util.List;

public record GraphDataResponse(List<GraphNodeResponse> nodes, List<GraphEdgeResponse> edges) {
}
