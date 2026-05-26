package com.synapse.knowledge.graph.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/graphs")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping
    public ApiResponse<GraphDataResponse> getGraphData(@CurrentUserAuth CurrentUser currentUser) {
        return ApiResponse.success(graphService.getGraphData(currentUser.userId()));
    }

    @GetMapping("/neighbors")
    public ApiResponse<GraphDataResponse> getNeighborGraph(
            @RequestParam Long noteId,
            @RequestParam(defaultValue = "2") int depth) {
        return ApiResponse.success(graphService.getNeighborGraph(noteId, depth));
    }
}
