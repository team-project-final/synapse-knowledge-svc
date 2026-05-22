package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.SearchModuleApi;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService implements SearchModuleApi {

    private final NoteSearchRepository noteSearchRepository;

    @Override
    public SearchPageResponse search(Long userId, SearchRequest request) {
        return noteSearchRepository.search(userId, request);
    }
}
