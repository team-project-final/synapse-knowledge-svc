package com.synapse.knowledge.search;

import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;

public interface SearchModuleApi {
    SearchPageResponse search(Long userId, SearchRequest request);

    SemanticSearchResponse semanticSearch(SearchIdentity identity, SemanticSearchRequest request);

    HybridSearchResponse hybridSearch(SearchIdentity identity, HybridSearchRequest request);
}
