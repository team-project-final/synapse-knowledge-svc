package com.synapse.knowledge.note.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteIdentityQueryAdapterTest {

    @Mock
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Mock
    private NoteRepository noteRepository;

    @Test
    @DisplayName("findByExternalNoteId_매핑없음_should빈옵셔널반환")
    void findByExternalNoteId_매핑없음_should빈옵셔널반환() {
        NoteIdentityQueryAdapter adapter = new NoteIdentityQueryAdapter(noteIdentityMapRepository, noteRepository);
        UUID externalNoteId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(noteIdentityMapRepository.findByExternalNoteId(externalNoteId)).thenReturn(Optional.empty());

        assertThat(adapter.findByExternalNoteId(externalNoteId)).isEmpty();
    }

    @Test
    @DisplayName("findByExternalNoteId_노트없음_should빈옵셔널반환")
    void findByExternalNoteId_노트없음_should빈옵셔널반환() {
        NoteIdentityQueryAdapter adapter = new NoteIdentityQueryAdapter(noteIdentityMapRepository, noteRepository);
        UUID externalNoteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        NoteIdentityMap identityMap = newIdentityMap(5L, externalNoteId);
        when(noteIdentityMapRepository.findByExternalNoteId(externalNoteId)).thenReturn(Optional.of(identityMap));
        when(noteRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.empty());

        assertThat(adapter.findByExternalNoteId(externalNoteId)).isEmpty();
    }

    @Test
    @DisplayName("findByExternalNoteId_정상매핑_should뷰반환")
    void findByExternalNoteId_정상매핑_should뷰반환() {
        NoteIdentityQueryAdapter adapter = new NoteIdentityQueryAdapter(noteIdentityMapRepository, noteRepository);
        UUID externalNoteId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        NoteIdentityMap identityMap = newIdentityMap(7L, externalNoteId);
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of("tag"));
        ReflectionTestUtils.setField(note, "id", 7L);

        when(noteIdentityMapRepository.findByExternalNoteId(externalNoteId)).thenReturn(Optional.of(identityMap));
        when(noteRepository.findByIdAndDeletedAtIsNull(7L)).thenReturn(Optional.of(note));

        var result = adapter.findByExternalNoteId(externalNoteId);

        assertThat(result).isPresent();
        assertThat(result.get().noteId()).isEqualTo(7L);
        assertThat(result.get().externalNoteId()).isEqualTo(externalNoteId);
        assertThat(result.get().title()).isEqualTo("제목");
    }

    private NoteIdentityMap newIdentityMap(Long noteId, UUID externalNoteId) {
        NoteIdentityMap identityMap = NoteIdentityMap.create(noteId);
        ReflectionTestUtils.setField(identityMap, "noteId", noteId);
        ReflectionTestUtils.setField(identityMap, "externalNoteId", externalNoteId);
        return identityMap;
    }
}
