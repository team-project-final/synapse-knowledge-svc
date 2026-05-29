package com.synapse.knowledge.search.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.service.SearchService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SearchService searchService;

    @Test
    @DisplayName("semantic_인증없음_shouldReturn401")
    void semantic_인증없음_shouldReturn401() throws Exception {
        SemanticSearchRequest request = new SemanticSearchRequest("시맨틱", 10, List.of("ai"));

        mockMvc.perform(
                post("/api/v1/ai/search/semantic")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("semantic_정상JWT요청_shouldReturn200")
    void semantic_정상JWT요청_shouldReturn200() throws Exception {
        String subject = UUID.randomUUID().toString();
        SemanticSearchRequest request = new SemanticSearchRequest("시맨틱", 10, List.of("ai"));
        SemanticSearchResponse.SemanticSearchResult result = new SemanticSearchResponse.SemanticSearchResult(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "요약",
            0.82f
        );
        SemanticSearchResponse response = new SemanticSearchResponse(
            List.of(result),
            1L,
            45L
        );
        given(searchService.semanticSearch(new SearchIdentity(100L, subject), request)).willReturn(response);

        mockMvc.perform(
                post("/api/v1/ai/search/semantic")
                    .with(jwt().jwt(jwt -> jwt.subject(subject).claim("userId", 100L)))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.results[0].chunkId").value(result.chunkId().toString()))
            .andExpect(jsonPath("$.data.results[0].noteId").value(result.noteId().toString()))
            .andExpect(jsonPath("$.data.results[0].score").value(0.82f));

        verify(searchService).semanticSearch(eq(new SearchIdentity(100L, subject)), eq(request));
    }

    @Test
    @DisplayName("hybrid_정상JWT요청_shouldReturn200")
    void hybrid_정상JWT요청_shouldReturn200() throws Exception {
        String subject = UUID.randomUUID().toString();
        HybridSearchRequest request = new HybridSearchRequest("하이브리드", 10, List.of("backend"));
        HybridSearchResponse response = HybridSearchResponse.of(
            List.of(new UnifiedSearchResultResponse(11L, "하이브리드 노트", List.of("<mark>하이브리드</mark>"), "요약", 3.4f, 0.91f, 0.032f)),
            70L,
            false
        );
        given(searchService.hybridSearch(new SearchIdentity(100L, subject), request)).willReturn(response);

        mockMvc.perform(
                post("/api/v1/ai/search/hybrid")
                    .with(jwt().jwt(jwt -> jwt.subject(subject).claim("userId", 100L)))
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.results[0].noteId").value(11L))
            .andExpect(jsonPath("$.data.results[0].keywordScore").value(3.4f))
            .andExpect(jsonPath("$.data.semanticFallback").value(false));

        verify(searchService).hybridSearch(eq(new SearchIdentity(100L, subject)), eq(request));
    }
}
