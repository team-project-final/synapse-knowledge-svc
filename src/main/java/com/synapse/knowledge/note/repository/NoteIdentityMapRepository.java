package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.NoteIdentityMap;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteIdentityMapRepository extends JpaRepository<NoteIdentityMap, Long> {

    Optional<NoteIdentityMap> findByExternalNoteId(UUID externalNoteId);
}
