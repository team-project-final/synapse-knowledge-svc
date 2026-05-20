package com.synapse.knowledge.chunking.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteChunkRepository extends JpaRepository<NoteChunk, Long> {
    List<NoteChunk> findByNoteIdOrderByChunkIndex(Long noteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from NoteChunk noteChunk where noteChunk.noteId = :noteId")
    void deleteByNoteId(@Param("noteId") Long noteId);
}
