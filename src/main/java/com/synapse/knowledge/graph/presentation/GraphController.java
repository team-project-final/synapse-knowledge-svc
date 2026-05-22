package com.synapse.knowledge.graph.presentation;

import com.synapse.knowledge.graph.application.GraphService;
import com.synapse.knowledge.graph.dto.GraphDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/data")
    public ResponseEntity<GraphDataResponse> getGraphData(@RequestParam Long userId) {
        return ResponseEntity.ok(graphService.getGraphData(userId));
    }

    @GetMapping
    public ResponseEntity<GraphDataResponse> getNeighborGraph(
            @RequestParam Long noteId,
            @RequestParam(defaultValue = "2") int depth) {
        return ResponseEntity.ok(graphService.getNeighborGraph(noteId, depth));
    }
}
