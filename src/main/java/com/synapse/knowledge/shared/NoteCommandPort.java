package com.synapse.knowledge.shared;

import java.util.List;
import java.util.UUID;

public interface NoteCommandPort {

    NoteCommandResult upsert(NoteUpsertCommand command);

    record NoteUpsertCommand(
        String tenantId,
        Long userId,
        String title,
        String contentMd,
        List<String> tags
    ) {
    }

    record NoteCommandResult(
        Long noteId,
        UUID externalNoteId,
        String title
    ) {
    }
}
