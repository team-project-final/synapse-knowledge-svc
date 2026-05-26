package com.synapse.knowledge.search.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/search")
public class AiSearchController {

    private final SearchService searchService;

    @PostMapping("/semantic")
    public ApiResponse<SemanticSearchResponse> semantic(
        @CurrentUserAuth CurrentUser currentUser,
        @Valid @RequestBody SemanticSearchRequest request
    ) {
        return ApiResponse.success(searchService.semanticSearch(currentUser.userId(), request));
    }

    @PostMapping("/hybrid")
    public ApiResponse<HybridSearchResponse> hybrid(
        @CurrentUserAuth CurrentUser currentUser,
        @Valid @RequestBody HybridSearchRequest request
    ) {
        return ApiResponse.success(searchService.hybridSearch(currentUser.userId(), request));
    }
}
