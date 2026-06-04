package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SearchResultResponse;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private NoteSearchRepository noteSearchRepository;

    @Mock
    private SemanticSearchService semanticSearchService;

    @Mock
    private HybridSearchService hybridSearchService;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("정상 요청 시 Repository 결과를 반환한다")
    void search_validRequest_shouldReturnRepositoryResults() {
        // Given
        Long userId = 100L;
        SearchRequest request = new SearchRequest("스프링", null, 20, List.of("backend"));
        SearchPageResponse expected = new SearchPageResponse(
            List.of(new SearchResultResponse(1L, "Spring Note", List.of("<mark>스프링</mark>"), 1.2f)),
            1L,
            null,
            false
        );
        given(noteSearchRepository.searchKeyword(userId, request)).willReturn(expected);

        // When
        SearchPageResponse actual = searchService.search(userId, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(noteSearchRepository).searchKeyword(userId, request);
    }

    @Test
    @DisplayName("정상 요청 시 SemanticService 결과를 반환한다")
    void semanticSearch_validRequest_shouldReturnSemanticServiceResults() {
        // Given
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        SemanticSearchRequest request = new SemanticSearchRequest("벡터 검색", 10, List.of("ai"));
        SemanticSearchResponse expected = new SemanticSearchResponse(
            List.of(new SemanticSearchResponse.SemanticSearchResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "벡터 검색 요약",
                0.91f
            )),
            1L,
            25L
        );
        given(semanticSearchService.search(identity, request)).willReturn(expected);

        // When
        SemanticSearchResponse actual = searchService.semanticSearch(identity, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(semanticSearchService).search(identity, request);
    }

    @Test
    @DisplayName("정상 요청 시 HybridService 결과를 반환한다")
    void hybridSearch_validRequest_shouldReturnHybridServiceResults() {
        // Given
        SearchIdentity identity = new SearchIdentity(100L, UUID.randomUUID().toString());
        HybridSearchRequest request = new HybridSearchRequest("하이브리드 검색", 10, List.of("backend"));
        HybridSearchResponse expected = HybridSearchResponse.of(
            List.of(new UnifiedSearchResultResponse(1L, "하이브리드 노트", List.of("<mark>검색</mark>"), "snippet", 3.2f, 0.88f, 0.033f)),
            52L,
            false
        );
        given(hybridSearchService.search(identity, request)).willReturn(expected);

        // When
        HybridSearchResponse actual = searchService.hybridSearch(identity, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(hybridSearchService).search(identity, request);
    }
}
