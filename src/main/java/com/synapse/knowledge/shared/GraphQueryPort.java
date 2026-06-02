package com.synapse.knowledge.shared;

import java.util.List;

public interface GraphQueryPort {
    List<GraphNoteData> findAllNoteByUserId(Long userId);

    List<GraphLinkData> findAllLinksByUserId(Long userId);

    List<GraphLinkData> findLinksByNoteId(Long noteId);

    List<GraphNoteData> findNotesByIds(List<Long> noteIds);

    List<GraphLinkData> findNeighborLinksByDepth(Long noteId, int maxDepth);
}
