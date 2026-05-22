package com.synapse.knowledge.search.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SearchResultResponse;
import com.synapse.knowledge.search.service.SearchService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NoteSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @Test
    @DisplayName("search_인증없음_shouldReturn401")
    void search_인증없음_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/notes/search").param("q", "spring"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("search_정상JWT요청_shouldReturn200")
    void search_정상JWT요청_shouldReturn200() throws Exception {
        SearchPageResponse response = new SearchPageResponse(
            List.of(new SearchResultResponse(10L, "검색 노트", List.of("<mark>spring</mark>"), 2.5f)),
            1L,
            null,
            false
        );
        SearchRequest request = new SearchRequest("spring", null, 20, null);
        given(searchService.search(100L, request)).willReturn(response);

        mockMvc.perform(
                get("/api/v1/notes/search")
                    .with(jwt().jwt(jwt -> jwt.subject("100").claim("userId", 100L)))
                    .param("q", "spring")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.results[0].noteId").value(10L))
            .andExpect(jsonPath("$.data.results[0].title").value("검색 노트"))
            .andExpect(jsonPath("$.data.totalCount").value(1L))
            .andExpect(jsonPath("$.data.hasNext").value(false));

        verify(searchService).search(eq(100L), eq(request));
    }

    @Test
    @DisplayName("search_권한오류_shouldReturn403")
    void search_권한오류_shouldReturn403() throws Exception {
        given(searchService.search(eq(100L), eq(new SearchRequest("spring", null, 20, null))))
            .willThrow(new AccessDeniedException("접근 권한이 없습니다"));

        mockMvc.perform(
                get("/api/v1/notes/search")
                    .with(jwt().jwt(jwt -> jwt.subject("100").claim("userId", 100L)))
                    .param("q", "spring")
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("C403"));
    }
}
