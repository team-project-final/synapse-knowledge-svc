package com.synapse.knowledge.graph.application;

import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.graph.dto.GraphEdgeResponse;
import com.synapse.knowledge.graph.dto.GraphNodeResponse;
import com.synapse.knowledge.shared.GraphLinkData;
import com.synapse.knowledge.shared.GraphNoteData;
import com.synapse.knowledge.shared.GraphQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphService {

    private static final double DAMPING = 0.85;
    private static final int PAGE_RANK_ITERATIONS = 10;

    private final GraphQueryPort graphQueryPort;

    public GraphDataResponse getGraphData(Long userId) {
        List<GraphNoteData> notes = graphQueryPort.findAllNoteByUserId(userId);
        List<GraphLinkData> links = graphQueryPort.findAllLinksByUserId(userId);

        Set<Long> noteIds = notes.stream().map(GraphNoteData::id).collect(Collectors.toSet());

        Map<Long, Integer> inDegree = computeInDegree(noteIds, links);
        Map<Long, Double> pageRanks = computePageRank(noteIds, links);

        List<GraphNodeResponse> nodes = notes.stream()
                .map(note -> new GraphNodeResponse(
                        note.id(),
                        note.title(),
                        inDegree.getOrDefault(note.id(), 0),
                        pageRanks.getOrDefault(note.id(), 1.0 / notes.size())
                ))
                .toList();

        List<GraphEdgeResponse> edges = links.stream()
                .filter(link -> noteIds.contains(link.sourceNoteId()) && noteIds.contains(link.targetNoteId()))
                .map(link -> new GraphEdgeResponse(link.sourceNoteId(), link.targetNoteId(), "wikilink"))
                .toList();

        return new GraphDataResponse(nodes, edges);
    }

    private Map<Long, Integer> computeInDegree(Set<Long> noteIds, List<GraphLinkData> links) {
        Map<Long, Integer> inDegree = new HashMap<>();
        noteIds.forEach(id -> inDegree.put(id, 0));
        links.stream()
                .filter(link -> noteIds.contains(link.targetNoteId()))
                .forEach(link -> inDegree.merge(link.targetNoteId(), 1, Integer::sum));
        return inDegree;
    }

    private Map<Long, Double> computePageRank(Set<Long> noteIds, List<GraphLinkData> links) {
        int n = noteIds.size();
        if (n == 0) return Map.of();

        Map<Long, Double> rank = new HashMap<>();
        noteIds.forEach(id -> rank.put(id, 1.0 / n));

        Map<Long, List<Long>> outLinks = buildOutLinks(noteIds, links);

        Map<Long, Double> current = rank;
        for (int i = 0; i < PAGE_RANK_ITERATIONS; i++) {
            current = iterate(current, outLinks, noteIds, n);
        }
        return current;
    }

    private Map<Long, List<Long>> buildOutLinks(Set<Long> noteIds, List<GraphLinkData> links) {
        return links.stream()
                .filter(l -> noteIds.contains(l.sourceNoteId()) && noteIds.contains(l.targetNoteId()))
                .collect(Collectors.groupingBy(
                        GraphLinkData::sourceNoteId,
                        Collectors.mapping(GraphLinkData::targetNoteId, Collectors.toList())
                ));
    }

    private Map<Long, Double> iterate(Map<Long, Double> rank, Map<Long, List<Long>> outLinks,
                                      Set<Long> noteIds, int n) {
        Map<Long, Double> next = new HashMap<>();
        noteIds.forEach(id -> next.put(id, (1.0 - DAMPING) / n));

        outLinks.forEach((src, targets) -> {
            double contribution = DAMPING * rank.get(src) / targets.size();
            targets.forEach(tgt -> next.merge(tgt, contribution, Double::sum));
        });
        return next;
    }
}
