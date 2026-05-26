package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RrfMergeServiceTest {

    private final RrfMergeService rrfMergeService = new RrfMergeService();

    @Test
    @DisplayName("merge_BM25와시맨틱결과가겹치면_shouldRrf점수로재정렬")
    void merge_BM25와시맨틱결과가겹치면_shouldRrf점수로재정렬() {
        // Given
        List<SearchCandidate> keywordResults = List.of(
            new SearchCandidate(1L, "A", List.of("<mark>A</mark>"), "snippet-a", 10.0f, null),
            new SearchCandidate(2L, "B", List.of(), "snippet-b", 9.0f, null)
        );
        List<SearchCandidate> semanticResults = List.of(
            new SearchCandidate(2L, "B", List.of(), "snippet-b", null, 0.97f),
            new SearchCandidate(3L, "C", List.of(), "snippet-c", null, 0.95f)
        );

        // When
        List<UnifiedSearchResultResponse> merged = rrfMergeService.merge(keywordResults, semanticResults, 3, 60);

        // Then
        assertThat(merged).hasSize(3);
        assertThat(merged.get(0).noteId()).isEqualTo(2L);
        assertThat(merged.get(0).keywordScore()).isEqualTo(9.0f);
        assertThat(merged.get(0).semanticScore()).isEqualTo(0.97f);
        assertThat(merged.get(0).rrfScore()).isGreaterThan(merged.get(1).rrfScore());
    }

    @Test
    @DisplayName("merge_시맨틱결과가비면_should키워드결과만반환")
    void merge_시맨틱결과가비면_should키워드결과만반환() {
        // Given
        List<SearchCandidate> keywordResults = List.of(
            new SearchCandidate(1L, "A", List.of(), "snippet-a", 10.0f, null)
        );

        // When
        List<UnifiedSearchResultResponse> merged = rrfMergeService.merge(keywordResults, List.of(), 10, 60);

        // Then
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).noteId()).isEqualTo(1L);
        assertThat(merged.get(0).semanticScore()).isNull();
    }
}
