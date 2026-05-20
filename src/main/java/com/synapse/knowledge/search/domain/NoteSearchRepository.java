package com.synapse.knowledge.search.domain;

import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.infrastructure.NoteSearchDocument;

public interface NoteSearchRepository {
    SearchPageResponse search(Long userId, SearchRequest request);

    void upsert(NoteSearchDocument document);

    void deleteByNoteId(Long noteId);
}
