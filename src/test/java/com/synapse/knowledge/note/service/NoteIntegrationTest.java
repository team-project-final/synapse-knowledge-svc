package com.synapse.knowledge.note.service;

import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.shared.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Test
    void createNote_정상생성_shouldReturnSavedNote() {
        // Given
        Long userId = 100L;
        NoteCreateRequest request = new NoteCreateRequest("tenant1", "Title", "Content with <b>tags</b>");

        // When
        NoteResponse response = noteService.create(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.contentMd()).contains("tags");
        assertThat(response.contentPlain()).doesNotContain("<b>", "</b>");
    }

    @Test
    void findAll_본인노트조회_shouldReturnPagedNotes() {
        // Given
        Long userId = 100L;
        noteService.create(userId, new NoteCreateRequest("tenant1", "Title 1", "Content 1"));
        noteService.create(userId, new NoteCreateRequest("tenant1", "Title 2", "Content 2"));
        noteService.create(200L, new NoteCreateRequest("tenant1", "Other", "Other"));

        // When
        Page<NoteResponse> notes = noteService.findAll(userId, PageRequest.of(0, 10));

        // Then
        assertThat(notes.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getNote_본인노트조회_shouldReturnNote() {
        // Given
        Long userId = 100L;
        NoteResponse created = noteService.create(userId, new NoteCreateRequest("tenant1", "Title", "Content"));

        // When
        NoteResponse found = noteService.getById(userId, created.id());

        // Then
        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test
    void getNote_타인노트조회_shouldThrowAccessDenied() {
        // Given
        Long ownerId = 100L;
        Long otherId = 200L;
        NoteResponse created = noteService.create(ownerId, new NoteCreateRequest("tenant1", "Title", "Content"));

        // When & Then
        assertThatThrownBy(() -> noteService.getById(otherId, created.id()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteNote_소프트삭제_shouldNotBeFound() {
        // Given
        Long userId = 100L;
        NoteResponse created = noteService.create(userId, new NoteCreateRequest("tenant1", "Title", "Content"));

        // When
        noteService.delete(userId, created.id());

        // Then
        assertThatThrownBy(() -> noteService.getById(userId, created.id()))
            .hasMessageContaining("Note not found");
        
        Page<NoteResponse> notes = noteService.findAll(userId, PageRequest.of(0, 10));
        assertThat(notes.getTotalElements()).isZero();
    }
}
