package com.synapse.knowledge.search.internal;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.SearchModuleApi;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SearchModuleBootstrap implements SearchModuleApi {

    private final SearchService searchService;

    @Override
    public SearchPageResponse search(Long userId, SearchRequest request) {
        return searchService.search(userId, request);
    }

    @Override
    public SemanticSearchResponse semanticSearch(SearchIdentity identity, SemanticSearchRequest request) {
        return searchService.semanticSearch(identity, request);
    }

    @Override
    public HybridSearchResponse hybridSearch(SearchIdentity identity, HybridSearchRequest request) {
        return searchService.hybridSearch(identity, request);
    }
}
