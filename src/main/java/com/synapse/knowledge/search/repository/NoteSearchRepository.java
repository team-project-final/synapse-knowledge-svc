package com.synapse.knowledge.search.repository;

import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;

public interface NoteSearchRepository {
    SearchPageResponse search(Long userId, SearchRequest request);

    void upsert(NoteSearchDocument document);

    void deleteByNoteId(Long noteId);
}
