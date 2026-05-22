package com.synapse.knowledge.graph.dto;

public record GraphNodeResponse(Long id, String title, int linkCount, double pageRank) {
}
