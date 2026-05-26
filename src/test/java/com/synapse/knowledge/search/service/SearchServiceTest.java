package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SearchResultResponse;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
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
    @DisplayName("search_정상요청_shouldRepository결과를반환")
    void search_정상요청_shouldRepository결과를반환() {
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
    @DisplayName("semanticSearch_정상요청_shouldSemanticService결과를반환")
    void semanticSearch_정상요청_shouldSemanticService결과를반환() {
        // Given
        Long userId = 100L;
        SemanticSearchRequest request = new SemanticSearchRequest("벡터 검색", 10, List.of("ai"));
        SemanticSearchResponse expected = new SemanticSearchResponse(
            List.of(new UnifiedSearchResultResponse(1L, "벡터 노트", List.of(), "벡터 검색 요약", null, 0.91f, 0.91f)),
            1L,
            25L
        );
        given(semanticSearchService.search(userId, request)).willReturn(expected);

        // When
        SemanticSearchResponse actual = searchService.semanticSearch(userId, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(semanticSearchService).search(userId, request);
    }

    @Test
    @DisplayName("hybridSearch_정상요청_shouldHybridService결과를반환")
    void hybridSearch_정상요청_shouldHybridService결과를반환() {
        // Given
        Long userId = 100L;
        HybridSearchRequest request = new HybridSearchRequest("하이브리드 검색", 10, List.of("backend"));
        HybridSearchResponse expected = HybridSearchResponse.of(
            List.of(new UnifiedSearchResultResponse(1L, "하이브리드 노트", List.of("<mark>검색</mark>"), "snippet", 3.2f, 0.88f, 0.033f)),
            52L,
            false
        );
        given(hybridSearchService.search(userId, request)).willReturn(expected);

        // When
        HybridSearchResponse actual = searchService.hybridSearch(userId, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(hybridSearchService).search(userId, request);
    }
}
