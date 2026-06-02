package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.NoteVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {

    List<NoteVersion> findByNoteIdOrderByVersionNoDesc(Long noteId);

    Optional<NoteVersion> findByNoteIdAndVersionNo(Long noteId, Integer versionNo);

    int countByNoteId(Long noteId);

    @Query("SELECT COALESCE(MAX(v.versionNo), 0) FROM NoteVersion v WHERE v.noteId = :noteId")
    int findMaxVersionNoByNoteId(@Param("noteId") Long noteId);

    @Query("SELECT COALESCE(MIN(v.versionNo), 0) FROM NoteVersion v WHERE v.noteId = :noteId")
    int findMinVersionNoByNoteId(@Param("noteId") Long noteId);

    void deleteByNoteIdAndVersionNo(Long noteId, Integer versionNo);
}
