package com.synapse.knowledge.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.search.domain.NoteSearchRepository;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SearchResultResponse;
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
        given(noteSearchRepository.search(userId, request)).willReturn(expected);

        // When
        SearchPageResponse actual = searchService.search(userId, request);

        // Then
        assertThat(actual).isEqualTo(expected);
        verify(noteSearchRepository).search(userId, request);
    }
}
