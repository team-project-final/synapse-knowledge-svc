package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Test
    void createNote_정상생성_shouldReturnSavedNote() {
        Long userId = 100L;
        NoteCreateRequest request = new NoteCreateRequest("tenant1", "Title", "Content with <b>tags</b>");

        NoteResponse response = noteService.create(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.contentMd()).contains("tags");
        assertThat(response.contentPlain()).doesNotContain("<b>", "</b>");
        assertThat(response.deckId()).isNull();
        assertThat(noteIdentityMapRepository.findById(response.id()))
            .isPresent()
            .get()
            .extracting(mapping -> mapping.getExternalNoteId())
            .isNotNull();
    }

    @Test
    void createNote_deckId포함_shouldPersistDeckId() {
        Long userId = 100L;
        String deckId = "550e8400-e29b-41d4-a716-446655440000";
        NoteCreateRequest request = new NoteCreateRequest("tenant1", "Deck Note", "Content", java.util.List.of(), deckId);

        NoteResponse response = noteService.create(userId, request);

        assertThat(response.deckId()).isEqualTo(deckId);
    }

    @Test
    void findAll_본인노트조회_shouldReturnPagedNotes() {
        Long userId = 100L;
        noteService.create(userId, new NoteCreateRequest("tenant1", "Title 1", "Content 1"));
        noteService.create(userId, new NoteCreateRequest("tenant1", "Title 2", "Content 2"));
        noteService.create(200L, new NoteCreateRequest("tenant1", "Other", "Other"));

        Page<NoteResponse> notes = noteService.findAll(userId, PageRequest.of(0, 10));

        assertThat(notes.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getNote_본인노트조회_shouldReturnNote() {
        Long userId = 100L;
        NoteResponse created = noteService.create(userId, new NoteCreateRequest("tenant1", "Title", "Content"));

        NoteResponse found = noteService.getById(userId, created.id());

        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test
    void getNote_타인노트조회_shouldThrowAccessDenied() {
        Long ownerId = 100L;
        Long otherId = 200L;
        NoteResponse created = noteService.create(ownerId, new NoteCreateRequest("tenant1", "Title", "Content"));

        assertThatThrownBy(() -> noteService.getById(otherId, created.id()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteNote_소프트삭제_shouldNotBeFound() {
        Long userId = 100L;
        NoteResponse created = noteService.create(userId, new NoteCreateRequest("tenant1", "Title", "Content"));

        noteService.delete(userId, created.id());

        assertThatThrownBy(() -> noteService.getById(userId, created.id()))
            .hasMessageContaining("Note not found");

        Page<NoteResponse> notes = noteService.findAll(userId, PageRequest.of(0, 10));
        assertThat(notes.getTotalElements()).isZero();
    }
}
