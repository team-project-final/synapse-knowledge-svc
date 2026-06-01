package com.synapse.knowledge.note.service.support;

import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.shared.NoteCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NoteCommandAdapter implements NoteCommandPort {

    private final NoteService noteService;
    private final NoteRepository noteRepository;
    private final NoteIdentityMapRepository noteIdentityMapRepository;

    @Override
    @Transactional
    public NoteCommandResult upsert(NoteUpsertCommand command) {
        Note note = noteRepository.findByTenantIdAndTitleAndDeletedAtIsNull(command.tenantId(), command.title())
            .orElse(null);

        Long noteId = note == null
            ? noteService.create(
                command.userId(),
                new NoteCreateRequest(command.tenantId(), command.title(), command.contentMd(), command.tags())
            ).id()
            : noteService.update(
                command.userId(),
                note.getId(),
                new NoteCreateRequest(command.tenantId(), command.title(), command.contentMd(), command.tags())
            ).id();

        NoteIdentityMap identityMap = noteIdentityMapRepository.findById(noteId)
            .orElseThrow(() -> new IllegalStateException("note identity mapping not found: " + noteId));

        return new NoteCommandResult(noteId, identityMap.getExternalNoteId(), command.title());
    }
}
