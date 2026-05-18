package com.synapse.knowledge.note.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteLinkRepository extends JpaRepository<NoteLink, Long> {
    void deleteBySourceNoteId(Long sourceNoteId);
    List<NoteLink> findByTargetNoteId(Long targetNoteId);
}
