package com.synapse.knowledge.search.internal;

import com.synapse.knowledge.search.SearchModuleApi;
import com.synapse.knowledge.search.service.SearchService;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
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
}
