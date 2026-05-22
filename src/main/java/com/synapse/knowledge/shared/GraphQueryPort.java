package com.synapse.knowledge.shared;

import java.util.List;

public interface GraphQueryPort {
    List<GraphNoteData> findAllNoteByUserId(Long userId);

    List<GraphLinkData> findAllLinksByUserId(Long userId);

}
