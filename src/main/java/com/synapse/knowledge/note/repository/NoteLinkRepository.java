package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.NoteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {
    void deleteBySourceNoteId(Long sourceNoteId);
    List<NoteLink> findBySourceNoteId(Long sourceNoteId);
    List<NoteLink> findByTargetNoteId(Long targetNoteId);
    List<NoteLink> findBySourceNoteIdIn(Collection<Long> sourceNoteIds);

    @Query(value = """
        WITH RECURSIVE graph_traversal(source_id, target_id, depth) AS (
            SELECT source_note_id, target_note_id, 1
            FROM note_links
            WHERE source_note_id = :noteId AND target_note_id IS NOT NULL
            UNION ALL
            SELECT nl.source_note_id, nl.target_note_id, gt.depth + 1
            FROM note_links nl
            INNER JOIN graph_traversal gt ON nl.source_note_id = gt.target_id
            WHERE gt.depth < :maxDepth AND nl.target_note_id IS NOT NULL
        )
        SELECT DISTINCT source_id AS source_note_id, target_id AS target_note_id
        FROM graph_traversal
        """, nativeQuery = true)
    List<Object[]> findNeighborLinksByDepthNative(@Param("noteId") Long noteId, @Param("maxDepth") int maxDepth);
}
