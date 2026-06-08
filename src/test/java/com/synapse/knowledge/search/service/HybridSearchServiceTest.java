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
import com.synapse.knowledge.shared.NoteIdentityQueryPort;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock
    private NoteIdentityQueryPort noteIdentityQueryPort;

    private final SearchProperties searchProperties = new SearchProperties(
        new SearchProperties.Ai("http://localhost:8090", Duration.ofSeconds(3), 0.55d),
        new SearchProperties.Hybrid(40, 5),
        new SearchProperties.Accuracy("test-v1", 910000L, "benchmark-search", "11111111-1111-1111-1111-111111111111", 10, Duration.ofSeconds(5)),
        new SearchProperties.Bm25(1.4d, 0.65d, 4.0d, 1.0d, 2.5d, "70%")
    );

    @Test
    @DisplayName("시맨틱 조회가 실패하면 BM25 Fallback으로 반환한다")
    void search_semanticQueryFails_shouldReturnBM25Fallback() {
        // Given
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        HybridSearchRequest request = new HybridSearchRequest("스프링", 10, null);
        UUID externalNoteId = UUID.randomUUID();
        List<SearchCandidate> keywordCandidates = List.of(
            new SearchCandidate(1L, externalNoteId, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null)
        );
        List<UnifiedSearchResultResponse> mergedResults = List.of(
            new UnifiedSearchResultResponse(1L, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null, 0.016f)
        );
        HybridSearchService hybridSearchService = new HybridSearchService(
            noteSearchRepository,
            learningAiSearchClient,
            rrfMergeService,
            searchProperties,
            noteIdentityQueryPort
        );

        given(noteSearchRepository.searchKeywordCandidates(100L, "스프링", 50, null)).willReturn(keywordCandidates);
        given(learningAiSearchClient.searchSemantic(identity.semanticActorId(), "스프링", 50))
            .willThrow(new IllegalStateException("semantic unavailable"));
        given(rrfMergeService.merge(keywordCandidates, List.of(), 10, 40)).willReturn(mergedResults);

        // When
        HybridSearchResponse response = hybridSearchService.search(identity, request);

        // Then
        assertThat(response.semanticFallback()).isTrue();
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).noteId()).isEqualTo(1L);
        verify(noteSearchRepository).searchKeywordCandidates(100L, "스프링", 50, null);
        verify(rrfMergeService).merge(keywordCandidates, List.of(), 10, 40);
    }

    @Test
    @DisplayName("시맨틱 UUID 매핑이 가능하면 병합 후 fallback false를 반환한다")
    void search_semanticUuidMappable_shouldMergeResultsWithFallbackFalse() {
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        HybridSearchRequest request = new HybridSearchRequest("스프링", 10, null);
        UUID keywordExternalNoteId = UUID.randomUUID();
        UUID semanticExternalNoteId = UUID.randomUUID();
        List<SearchCandidate> keywordCandidates = List.of(
            new SearchCandidate(1L, keywordExternalNoteId, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null)
        );
        List<LearningAiSearchClient.LearningAiSemanticHit> semanticHits = List.of(
            new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), semanticExternalNoteId, "의미상 관련", 0.98f)
        );
        List<SearchCandidate> semanticCandidates = List.of(
            new SearchCandidate(2L, semanticExternalNoteId, "의미 노트", List.of(), "의미상 관련", null, 0.98f)
        );
        List<UnifiedSearchResultResponse> mergedResults = List.of(
            new UnifiedSearchResultResponse(2L, "의미 노트", List.of(), "의미상 관련", null, 0.98f, 0.016f)
        );
        HybridSearchService hybridSearchService = new HybridSearchService(
            noteSearchRepository,
            learningAiSearchClient,
            rrfMergeService,
            searchProperties,
            noteIdentityQueryPort
        );

        given(noteSearchRepository.searchKeywordCandidates(100L, "스프링", 50, null)).willReturn(keywordCandidates);
        given(learningAiSearchClient.searchSemantic(identity.semanticActorId(), "스프링", 50)).willReturn(semanticHits);
        given(noteIdentityQueryPort.findByExternalNoteId(semanticExternalNoteId))
            .willReturn(java.util.Optional.of(new NoteIdentityQueryPort.NoteIdentityView(2L, semanticExternalNoteId, "의미 노트")));
        given(rrfMergeService.merge(keywordCandidates, semanticCandidates, 10, 40)).willReturn(mergedResults);

        HybridSearchResponse response = hybridSearchService.search(identity, request);

        assertThat(response.semanticFallback()).isFalse();
        assertThat(response.results()).containsExactlyElementsOf(mergedResults);
        verify(noteIdentityQueryPort).findByExternalNoteId(semanticExternalNoteId);
        verify(rrfMergeService).merge(keywordCandidates, semanticCandidates, 10, 40);
    }

    @Test
    @DisplayName("같은 노트의 시맨틱 hit가 여러 개면 최고 점수 한 건으로 축약한다")
    void search_duplicateSemanticHitsForSameNote_shouldDeduplicateByExternalNoteId() {
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        HybridSearchRequest request = new HybridSearchRequest("스프링", 10, null);
        UUID keywordExternalNoteId = UUID.randomUUID();
        UUID semanticExternalNoteId = UUID.randomUUID();
        List<SearchCandidate> keywordCandidates = List.of(
            new SearchCandidate(1L, keywordExternalNoteId, "스프링 노트", List.of("<mark>스프링</mark>"), "snippet", 4.2f, null)
        );
        List<LearningAiSearchClient.LearningAiSemanticHit> semanticHits = List.of(
            new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), semanticExternalNoteId, "낮은 점수", 0.74f),
            new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), semanticExternalNoteId, "높은 점수", 0.98f)
        );
        List<SearchCandidate> semanticCandidates = List.of(
            new SearchCandidate(2L, semanticExternalNoteId, "의미 노트", List.of(), "높은 점수", null, 0.98f)
        );
        List<UnifiedSearchResultResponse> mergedResults = List.of(
            new UnifiedSearchResultResponse(2L, "의미 노트", List.of(), "높은 점수", null, 0.98f, 0.04f)
        );
        HybridSearchService hybridSearchService = new HybridSearchService(
            noteSearchRepository,
            learningAiSearchClient,
            rrfMergeService,
            searchProperties,
            noteIdentityQueryPort
        );

        given(noteSearchRepository.searchKeywordCandidates(100L, "스프링", 50, null)).willReturn(keywordCandidates);
        given(learningAiSearchClient.searchSemantic(identity.semanticActorId(), "스프링", 50)).willReturn(semanticHits);
        given(noteIdentityQueryPort.findByExternalNoteId(semanticExternalNoteId))
            .willReturn(java.util.Optional.of(new NoteIdentityQueryPort.NoteIdentityView(2L, semanticExternalNoteId, "의미 노트")));
        given(rrfMergeService.merge(keywordCandidates, semanticCandidates, 10, 40)).willReturn(mergedResults);

        HybridSearchResponse response = hybridSearchService.search(identity, request);

        assertThat(response.semanticFallback()).isFalse();
        assertThat(response.results()).containsExactlyElementsOf(mergedResults);
        verify(rrfMergeService).merge(keywordCandidates, semanticCandidates, 10, 40);
    }
}
