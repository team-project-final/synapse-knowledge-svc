package com.synapse.knowledge.note.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.shared.NoteCommandPort.NoteUpsertCommand;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteCommandAdapterTest {

    @Mock
    private NoteService noteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Test
    @DisplayName("upsert_신규노트_should생성후매핑반환")
    void upsert_신규노트_should생성후매핑반환() {
        NoteCommandAdapter adapter = new NoteCommandAdapter(noteService, noteRepository, noteIdentityMapRepository);
        NoteUpsertCommand command = new NoteUpsertCommand("tenant-1", 10L, "새 노트", "본문", List.of("tag"));
        NoteIdentityMap identityMap = newIdentityMap(1L, "11111111-1111-1111-1111-111111111111");
        Note savedNote = Note.create("tenant-1", 10L, "새 노트", "본문", "본문", List.of("tag"));
        ReflectionTestUtils.setField(savedNote, "id", 1L);

        when(noteRepository.findByTenantIdAndTitleAndDeletedAtIsNull("tenant-1", "새 노트"))
            .thenReturn(Optional.empty());
        when(noteService.create(eq(10L), any(NoteCreateRequest.class)))
            .thenReturn(NoteResponse.from(savedNote));
        when(noteIdentityMapRepository.findById(1L)).thenReturn(Optional.of(identityMap));

        var result = adapter.upsert(command);

        verify(noteService).create(10L, new NoteCreateRequest("tenant-1", "새 노트", "본문", List.of("tag")));
        assertThat(result.noteId()).isEqualTo(1L);
        assertThat(result.externalNoteId()).isEqualTo(identityMap.getExternalNoteId());
        assertThat(result.title()).isEqualTo("새 노트");
    }

    @Test
    @DisplayName("upsert_기존노트_should수정후매핑반환")
    void upsert_기존노트_should수정후매핑반환() {
        NoteCommandAdapter adapter = new NoteCommandAdapter(noteService, noteRepository, noteIdentityMapRepository);
        Note existingNote = Note.create("tenant-1", 10L, "기존 노트", "이전", "이전", List.of("old"));
        NoteIdentityMap identityMap = newIdentityMap(5L, "22222222-2222-2222-2222-222222222222");
        ReflectionTestUtils.setField(existingNote, "id", 5L);
        existingNote.update("기존 노트", "수정 본문", "수정 본문", List.of("new"));

        when(noteRepository.findByTenantIdAndTitleAndDeletedAtIsNull("tenant-1", "기존 노트"))
            .thenReturn(Optional.of(existingNote));
        when(noteService.update(eq(10L), eq(5L), any(NoteCreateRequest.class)))
            .thenReturn(NoteResponse.from(existingNote));
        when(noteIdentityMapRepository.findById(5L)).thenReturn(Optional.of(identityMap));

        var result = adapter.upsert(new NoteUpsertCommand("tenant-1", 10L, "기존 노트", "수정 본문", List.of("new")));

        verify(noteService).update(10L, 5L, new NoteCreateRequest("tenant-1", "기존 노트", "수정 본문", List.of("new")));
        assertThat(result.noteId()).isEqualTo(5L);
        assertThat(result.externalNoteId()).isEqualTo(identityMap.getExternalNoteId());
        assertThat(result.title()).isEqualTo("기존 노트");
    }

    @Test
    @DisplayName("upsert_매핑없음_should예외발생")
    void upsert_매핑없음_should예외발생() {
        NoteCommandAdapter adapter = new NoteCommandAdapter(noteService, noteRepository, noteIdentityMapRepository);

        when(noteRepository.findByTenantIdAndTitleAndDeletedAtIsNull("tenant-1", "새 노트"))
            .thenReturn(Optional.empty());
        Note savedNote = Note.create("tenant-1", 10L, "새 노트", "본문", "본문", List.of());
        ReflectionTestUtils.setField(savedNote, "id", 1L);
        when(noteService.create(eq(10L), any(NoteCreateRequest.class)))
            .thenReturn(NoteResponse.from(savedNote));
        when(noteIdentityMapRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.upsert(new NoteUpsertCommand("tenant-1", 10L, "새 노트", "본문", List.of())))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("note identity mapping not found");
    }

    private NoteIdentityMap newIdentityMap(Long noteId, String externalNoteId) {
        NoteIdentityMap identityMap = NoteIdentityMap.create(noteId);
        ReflectionTestUtils.setField(identityMap, "noteId", noteId);
        ReflectionTestUtils.setField(identityMap, "externalNoteId", java.util.UUID.fromString(externalNoteId));
        return identityMap;
    }
}
