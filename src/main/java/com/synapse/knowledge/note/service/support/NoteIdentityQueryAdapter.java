package com.synapse.knowledge.note.service.support;

import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.shared.NoteIdentityQueryPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteIdentityQueryAdapter implements NoteIdentityQueryPort {

    private final NoteIdentityMapRepository noteIdentityMapRepository;
    private final NoteRepository noteRepository;

    @Override
    public Optional<NoteIdentityView> findByExternalNoteId(UUID externalNoteId) {
        Optional<NoteIdentityMap> mapping = noteIdentityMapRepository.findByExternalNoteId(externalNoteId);
        if (mapping.isEmpty()) {
            return Optional.empty();
        }

        Optional<Note> note = noteRepository.findByIdAndDeletedAtIsNull(mapping.get().getNoteId());
        if (note.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new NoteIdentityView(
            note.get().getId(),
            mapping.get().getExternalNoteId(),
            note.get().getTitle()
        ));
    }
}
