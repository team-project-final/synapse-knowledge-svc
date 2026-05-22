package com.synapse.knowledge.graph.presentation;

import com.synapse.knowledge.graph.application.GraphService;
import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.shared.CurrentUser;
import com.synapse.knowledge.shared.CurrentUserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/data")
    public ResponseEntity<GraphDataResponse> getGraphData(@CurrentUserAuth CurrentUser currentUser) {
        return ResponseEntity.ok(graphService.getGraphData(currentUser.userId()));
    }
}
