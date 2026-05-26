package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.SearchModuleApi;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchModuleApi {

    private final NoteSearchRepository noteSearchRepository;
    private final SemanticSearchService semanticSearchService;
    private final HybridSearchService hybridSearchService;

    @Override
    @Transactional(readOnly = true)
    public SearchPageResponse search(Long userId, SearchRequest request) {
        return noteSearchRepository.searchKeyword(userId, request);
    }

    @Override
    public SemanticSearchResponse semanticSearch(Long userId, SemanticSearchRequest request) {
        return semanticSearchService.search(userId, request);
    }

    @Override
    public HybridSearchResponse hybridSearch(Long userId, HybridSearchRequest request) {
        return hybridSearchService.search(userId, request);
    }
}
