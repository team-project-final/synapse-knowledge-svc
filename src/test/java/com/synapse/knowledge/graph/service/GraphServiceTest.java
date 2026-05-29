package com.synapse.knowledge.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.shared.GraphLinkData;
import com.synapse.knowledge.shared.GraphNoteData;
import com.synapse.knowledge.shared.GraphQueryPort;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private GraphQueryPort graphQueryPort;

    @Spy
    private GraphMapper graphMapper = Mappers.getMapper(GraphMapper.class);

    @InjectMocks
    private GraphService graphService;

    @Test
    @DisplayName("getGraphData_노드없음_shouldReturnEmptyGraph")
    void getGraphData_노드없음_shouldReturnEmptyGraph() {
        // Given
        given(graphQueryPort.findAllNoteByUserId(1L)).willReturn(List.of());
        given(graphQueryPort.findAllLinksByUserId(1L)).willReturn(List.of());

        // When
        GraphDataResponse response = graphService.getGraphData(1L);

        // Then
        assertThat(response.nodes()).isEmpty();
        assertThat(response.edges()).isEmpty();
    }

    @Test
    @DisplayName("getGraphData_링크있는노드_shouldComputeInDegree")
    void getGraphData_링크있는노드_shouldComputeInDegree() {
        // Given
        List<GraphNoteData> notes = List.of(
                new GraphNoteData(1L, "A", 1L),
                new GraphNoteData(2L, "B", 1L)
        );
        List<GraphLinkData> links = List.of(new GraphLinkData(1L, 2L));
        given(graphQueryPort.findAllNoteByUserId(1L)).willReturn(notes);
        given(graphQueryPort.findAllLinksByUserId(1L)).willReturn(links);

        // When
        GraphDataResponse response = graphService.getGraphData(1L);

        // Then
        assertThat(response.nodes()).hasSize(2);
        assertThat(response.edges()).hasSize(1);
        assertThat(response.nodes().stream().filter(n -> n.title().equals("B")).findFirst())
                .isPresent()
                .get()
                .satisfies(n -> assertThat(n.linkCount()).isEqualTo(1));
    }

    @Test
    @DisplayName("getGraphData_pageRank_shouldBePositive")
    void getGraphData_pageRank_shouldBePositive() {
        // Given
        List<GraphNoteData> notes = List.of(
                new GraphNoteData(1L, "Alpha", 1L),
                new GraphNoteData(2L, "Beta", 1L)
        );
        given(graphQueryPort.findAllNoteByUserId(1L)).willReturn(notes);
        given(graphQueryPort.findAllLinksByUserId(1L)).willReturn(List.of(new GraphLinkData(2L, 1L)));

        // When
        GraphDataResponse response = graphService.getGraphData(1L);

        // Then
        assertThat(response.nodes()).allMatch(n -> n.pageRank() > 0);
    }

    @Test
    @DisplayName("getGraphData_다른유저링크_shouldFilterCrossUserEdges")
    void getGraphData_다른유저링크_shouldFilterCrossUserEdges() {
        // Given — noteId=99 is not in user's notes
        List<GraphNoteData> notes = List.of(new GraphNoteData(1L, "Mine", 1L));
        List<GraphLinkData> links = List.of(new GraphLinkData(1L, 99L));
        given(graphQueryPort.findAllNoteByUserId(1L)).willReturn(notes);
        given(graphQueryPort.findAllLinksByUserId(1L)).willReturn(links);

        // When
        GraphDataResponse response = graphService.getGraphData(1L);

        // Then
        assertThat(response.edges()).isEmpty();
    }
}
