package com.synapse.knowledge.search;

import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;

public interface SearchModuleApi {
    SearchPageResponse search(Long userId, SearchRequest request);
}
