package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.dto.NoteShareableResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import java.util.List;
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
        assertThat(noteIdentityMapRepository.findById(response.id()))
            .isPresent()
            .get()
            .extracting(mapping -> mapping.getExternalNoteId())
            .isNotNull();
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
    void checkShareable_본인노트_shouldReturnShareable() {
        Long userId = 100L;
        NoteResponse created = noteService.create(
            userId, new NoteCreateRequest("tenant1", "Title", "Content body", List.of("tag1", "tag2")));

        NoteShareableResponse response = noteService.checkShareable(userId, created.id());

        assertThat(response.shareable()).isTrue();
        assertThat(response.noteId()).isEqualTo(created.id());
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.description()).contains("Content body");
        assertThat(response.tags()).containsExactly("tag1", "tag2");
        assertThat(response.reason()).isNull();
    }

    @Test
    void checkShareable_타인노트_shouldReturnNotOwner() {
        Long ownerId = 100L;
        Long otherId = 200L;
        NoteResponse created = noteService.create(ownerId, new NoteCreateRequest("tenant1", "Title", "Content"));

        NoteShareableResponse response = noteService.checkShareable(otherId, created.id());

        assertThat(response.shareable()).isFalse();
        assertThat(response.reason()).isEqualTo("NOT_OWNER");
        assertThat(response.title()).isNull();
    }

    @Test
    void checkShareable_없는노트_shouldReturnNotFound() {
        NoteShareableResponse response = noteService.checkShareable(100L, 999999L);

        assertThat(response.shareable()).isFalse();
        assertThat(response.reason()).isEqualTo("NOT_FOUND");
    }

    @Test
    void checkShareable_삭제된노트_shouldReturnNotFound() {
        Long userId = 100L;
        NoteResponse created = noteService.create(userId, new NoteCreateRequest("tenant1", "Title", "Content"));
        noteService.delete(userId, created.id());

        NoteShareableResponse response = noteService.checkShareable(userId, created.id());

        assertThat(response.shareable()).isFalse();
        assertThat(response.reason()).isEqualTo("NOT_FOUND");
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
