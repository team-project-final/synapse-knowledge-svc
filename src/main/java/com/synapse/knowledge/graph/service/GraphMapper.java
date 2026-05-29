package com.synapse.knowledge.graph.service;

import com.synapse.knowledge.graph.dto.GraphEdgeResponse;
import com.synapse.knowledge.graph.dto.GraphNodeResponse;
import com.synapse.knowledge.shared.GraphLinkData;
import com.synapse.knowledge.shared.GraphNoteData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GraphMapper {

    @Mapping(source = "note.id", target = "id")
    @Mapping(source = "note.title", target = "title")
    @Mapping(source = "linkCount", target = "linkCount")
    @Mapping(source = "pageRank", target = "pageRank")
    GraphNodeResponse toNodeResponse(GraphNoteData note, int linkCount, double pageRank);

    @Mapping(source = "sourceNoteId", target = "source")
    @Mapping(source = "targetNoteId", target = "target")
    @Mapping(target = "type", constant = "wikilink")
    GraphEdgeResponse toEdgeResponse(GraphLinkData link);
}
