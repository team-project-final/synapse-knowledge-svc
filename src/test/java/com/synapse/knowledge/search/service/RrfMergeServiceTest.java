package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RrfMergeServiceTest {

    private final RrfMergeService rrfMergeService = new RrfMergeService();

    @Test
    @DisplayName("BM25와 시맨틱 결과가 겹치면 RRF 점수로 재정렬한다")
    void merge_bm25AndSemanticResultsOverlap_shouldReorderByRrfScore() {
        // Given
        UUID noteA = UUID.randomUUID();
        UUID noteB = UUID.randomUUID();
        UUID noteC = UUID.randomUUID();
        List<SearchCandidate> keywordResults = List.of(
            new SearchCandidate(1L, noteA, "A", List.of("<mark>A</mark>"), "snippet-a", 10.0f, null),
            new SearchCandidate(2L, noteB, "B", List.of(), "snippet-b", 9.0f, null)
        );
        List<SearchCandidate> semanticResults = List.of(
            new SearchCandidate(2L, noteB, "B", List.of(), "snippet-b", null, 0.97f),
            new SearchCandidate(3L, noteC, "C", List.of(), "snippet-c", null, 0.95f)
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
    @DisplayName("시맨틱 결과가 비어있으면 키워드 결과만 반환한다")
    void merge_semanticResultsEmpty_shouldReturnOnlyKeywordResults() {
        // Given
        List<SearchCandidate> keywordResults = List.of(
            new SearchCandidate(1L, UUID.randomUUID(), "A", List.of(), "snippet-a", 10.0f, null)
        );

        // When
        List<UnifiedSearchResultResponse> merged = rrfMergeService.merge(keywordResults, List.of(), 10, 60);

        // Then
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).noteId()).isEqualTo(1L);
        assertThat(merged.get(0).semanticScore()).isNull();
    }

    @Test
    @DisplayName("같은 source 안에서 중복 노트는 한 번만 RRF 점수에 반영한다")
    void merge_duplicateResultsFromSameSource_shouldScoreOnlyOncePerSource() {
        UUID noteA = UUID.randomUUID();
        UUID noteB = UUID.randomUUID();
        List<SearchCandidate> keywordResults = List.of(
            new SearchCandidate(1L, noteA, "A", List.of(), "snippet-a", 10.0f, null),
            new SearchCandidate(2L, noteB, "B", List.of(), "snippet-b", 9.0f, null)
        );
        List<SearchCandidate> semanticResults = List.of(
            new SearchCandidate(2L, noteB, "B", List.of(), "snippet-b-1", null, 0.99f),
            new SearchCandidate(2L, noteB, "B", List.of(), "snippet-b-2", null, 0.97f)
        );

        List<UnifiedSearchResultResponse> merged = rrfMergeService.merge(keywordResults, semanticResults, 3, 40);

        assertThat(merged).hasSize(2);
        assertThat(merged.get(0).noteId()).isEqualTo(2L);
        assertThat(merged.get(0).rrfScore()).isCloseTo((float) ((1.0d / 42.0d) + (1.0d / 41.0d)), org.assertj.core.data.Offset.offset(0.000001f));
    }
}
