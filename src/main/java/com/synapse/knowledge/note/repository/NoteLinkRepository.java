package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.NoteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {
    void deleteBySourceNoteId(Long sourceNoteId);
    List<NoteLink> findByTargetNoteId(Long targetNoteId);
    List<NoteLink> findBySourceNoteIdIn(Collection<Long> sourceNoteIds);
}
