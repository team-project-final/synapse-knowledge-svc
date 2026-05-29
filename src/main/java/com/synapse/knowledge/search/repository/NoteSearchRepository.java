package com.synapse.knowledge.search.repository;

import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.util.List;

public interface NoteSearchRepository {
    SearchPageResponse searchKeyword(Long userId, SearchRequest request);

    List<SearchCandidate> searchKeywordCandidates(Long userId, String query, int limit, List<String> tags);

    void upsert(NoteSearchDocument document);

    void deleteByNoteId(Long noteId);
}
