package com.synapse.knowledge.search.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.service.SearchService;
import jakarta.validation.Valid;
import java.util.UUID;
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
        return ApiResponse.success(searchService.semanticSearch(createSearchIdentity(currentUser), request));
    }

    @PostMapping("/hybrid")
    public ApiResponse<HybridSearchResponse> hybrid(
        @CurrentUserAuth CurrentUser currentUser,
        @Valid @RequestBody HybridSearchRequest request
    ) {
        return ApiResponse.success(searchService.hybridSearch(createSearchIdentity(currentUser), request));
    }

    private SearchIdentity createSearchIdentity(CurrentUser currentUser) {
        return new SearchIdentity(currentUser.userId(), toSemanticActorId(currentUser.subject()));
    }

    private String toSemanticActorId(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(subject).toString();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
