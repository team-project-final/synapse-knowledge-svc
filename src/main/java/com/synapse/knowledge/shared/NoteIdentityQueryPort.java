package com.synapse.knowledge.shared;

import java.util.Optional;
import java.util.UUID;

public interface NoteIdentityQueryPort {

    Optional<NoteIdentityView> findByExternalNoteId(UUID externalNoteId);

    record NoteIdentityView(
        Long noteId,
        UUID externalNoteId,
        String title
    ) {
    }
}
