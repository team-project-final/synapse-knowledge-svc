package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.time.Duration;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    @Mock
    private NoteSearchRepository noteSearchRepository;

    @Mock
    private LearningAiSearchClient learningAiSearchClient;

    @Mock
    private RrfMergeService rrfMergeService;

    private final SearchProperties searchProperties = new SearchProperties(
        new SearchProperties.Ai("http://localhost:8090", Duration.ofSeconds(3), 0.7d),
        new SearchProperties.Hybrid(60, 3)
    );

    @Test
    @DisplayName("search_시맨틱조회가실패하면_shouldBM25Fallback으로반환")
    void search_시맨틱조회가실패하면_shouldBM25Fallback으로반환() {
        // Given
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        HybridSearchRequest request = new HybridSearchRequest("스프링", 10, null);
        List<SearchCandidate> keywordCandidates = List.of(
            new SearchCandidate(1L, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null)
        );
        List<UnifiedSearchResultResponse> mergedResults = List.of(
            new UnifiedSearchResultResponse(1L, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null, 0.016f)
        );
        HybridSearchService hybridSearchService = new HybridSearchService(
            noteSearchRepository,
            learningAiSearchClient,
            rrfMergeService,
            searchProperties
        );

        given(noteSearchRepository.searchKeywordCandidates(100L, "스프링", 30, null)).willReturn(keywordCandidates);
        given(learningAiSearchClient.searchSemantic(identity.semanticActorId(), "스프링", 30))
            .willThrow(new IllegalStateException("semantic unavailable"));
        given(rrfMergeService.merge(keywordCandidates, List.of(), 10, 60)).willReturn(mergedResults);

        // When
        HybridSearchResponse response = hybridSearchService.search(identity, request);

        // Then
        assertThat(response.semanticFallback()).isTrue();
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).noteId()).isEqualTo(1L);
        verify(noteSearchRepository).searchKeywordCandidates(100L, "스프링", 30, null);
        verify(rrfMergeService).merge(keywordCandidates, List.of(), 10, 60);
    }
}
