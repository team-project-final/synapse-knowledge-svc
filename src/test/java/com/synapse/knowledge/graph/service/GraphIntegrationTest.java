package com.synapse.knowledge.graph.service;

import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.graph.dto.GraphNodeResponse;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GraphIntegrationTest {

    @Autowired
    private GraphService graphService;

    @Autowired
    private NoteService noteService;

    @Test
    @DisplayName("getGraphData_노드와엣지반환_shouldReturnNodesAndEdges")
    void getGraphData_노드와엣지반환_shouldReturnNodesAndEdges() {
        // Given: A → B, A → C 위키링크 구조
        Long userId = 1L;
        String tenantId = "tenant-1";

        noteService.create(userId, new NoteCreateRequest(tenantId, "Note B", "Content B"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note C", "Content C"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note A", "Links to [[Note B]] and [[Note C]]"));

        // When
        GraphDataResponse response = graphService.getGraphData(userId);

        // Then
        assertThat(response.nodes()).hasSize(3);
        assertThat(response.edges()).hasSize(2);

        List<String> titles = response.nodes().stream().map(GraphNodeResponse::title).toList();
        assertThat(titles).containsExactlyInAnyOrder("Note A", "Note B", "Note C");

        assertThat(response.edges()).allMatch(e -> e.type().equals("wikilink"));
    }

    @Test
    @DisplayName("getGraphData_linkCount_inDegree정확히계산")
    void getGraphData_linkCount_inDegree정확히계산() {
        // Given: A → C, B → C (C는 in-degree 2)
        Long userId = 1L;
        String tenantId = "tenant-1";

        noteService.create(userId, new NoteCreateRequest(tenantId, "Hub", "Popular note"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note A", "Links to [[Hub]]"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note B", "Also links to [[Hub]]"));

        // When
        GraphDataResponse response = graphService.getGraphData(userId);

        // Then
        GraphNodeResponse hub = response.nodes().stream()
                .filter(n -> n.title().equals("Hub"))
                .findFirst().orElseThrow();

        assertThat(hub.linkCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getGraphData_타사용자노드제외_shouldFilterByUserId")
    void getGraphData_타사용자노드제외_shouldFilterByUserId() {
        // Given
        Long userId = 1L;
        Long otherId = 2L;
        String tenantId = "tenant-1";

        noteService.create(userId, new NoteCreateRequest(tenantId, "My Note", "Content"));
        noteService.create(otherId, new NoteCreateRequest(tenantId, "Other Note", "Content"));

        // When
        GraphDataResponse response = graphService.getGraphData(userId);

        // Then
        assertThat(response.nodes()).hasSize(1);
        assertThat(response.nodes().get(0).title()).isEqualTo("My Note");
    }

    @Test
    @DisplayName("getGraphData_노트없음_shouldReturnEmptyGraph")
    void getGraphData_노트없음_shouldReturnEmptyGraph() {
        // When
        GraphDataResponse response = graphService.getGraphData(999L);

        // Then
        assertThat(response.nodes()).isEmpty();
        assertThat(response.edges()).isEmpty();
    }

    @Test
    @DisplayName("getGraphData_pageRank_양수값반환")
    void getGraphData_pageRank_양수값반환() {
        // Given
        Long userId = 1L;
        String tenantId = "tenant-1";

        noteService.create(userId, new NoteCreateRequest(tenantId, "Alpha", "Content"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Beta", "Links to [[Alpha]]"));

        // When
        GraphDataResponse response = graphService.getGraphData(userId);

        // Then
        assertThat(response.nodes()).allMatch(n -> n.pageRank() > 0);
    }
}
