package com.synapse.knowledge.graph.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.graph.dto.GraphEdgeResponse;
import com.synapse.knowledge.graph.dto.GraphNodeResponse;
import com.synapse.knowledge.graph.service.GraphService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GraphService graphService;

    @Test
    @DisplayName("getGraphData_인증없음_shouldReturn401")
    void getGraphData_인증없음_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/graph/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getNeighborGraph_인증없음_shouldReturn401")
    void getNeighborGraph_인증없음_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/graph/neighbor").param("noteId", "1").param("depth", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getGraphData_정상JWT요청_shouldReturn200")
    void getGraphData_정상JWT요청_shouldReturn200() throws Exception {
        GraphDataResponse response = new GraphDataResponse(
                List.of(new GraphNodeResponse(1L, "Note A", 2, 0.5)),
                List.of(new GraphEdgeResponse(1L, 2L, "wikilink"))
        );
        given(graphService.getGraphData(1L)).willReturn(response);

        mockMvc.perform(
                get("/api/graph/data")
                        .with(jwt().jwt(j -> j.subject("1").claim("userId", 1L)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[0].title").value("Note A"))
                .andExpect(jsonPath("$.data.edges[0].type").value("wikilink"));
    }

    @Test
    @DisplayName("getNeighborGraph_정상JWT요청_shouldReturn200")
    void getNeighborGraph_정상JWT요청_shouldReturn200() throws Exception {
        GraphDataResponse response = new GraphDataResponse(
                List.of(new GraphNodeResponse(1L, "Note A", 1, 0.3)),
                List.of()
        );
        given(graphService.getNeighborGraph(1L, 2)).willReturn(response);

        mockMvc.perform(
                get("/api/graph/neighbor")
                        .with(jwt().jwt(j -> j.subject("1").claim("userId", 1L)))
                        .param("noteId", "1")
                        .param("depth", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes[0].id").value(1L));
    }
}
