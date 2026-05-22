package com.synapse.knowledge.note.service;

import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteLinkIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Test
    @DisplayName("backlinks_자동매핑_shouldReturnSourceNotes")
    void backlinks_자동매핑_shouldReturnSourceNotes() {
        // Given
        Long userId = 1L;
        String tenantId = "tenant-1";
        
        // 1. 대상 노트 먼저 생성
        NoteResponse target = noteService.create(userId, new NoteCreateRequest(tenantId, "Spring", "Target Note"));
        
        // 2. 위키링크 포함한 소스 노트 생성
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note A", "I love [[Spring]]"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "Note B", "Learning [[Spring]] is fun"));

        // When
        List<NoteResponse> backlinks = noteService.getBacklinks(userId, target.id());

        // Then
        assertThat(backlinks).hasSize(2);
        assertThat(backlinks).extracting(NoteResponse::title)
                .containsExactlyInAnyOrder("Note A", "Note B");
    }

    @Test
    @DisplayName("links_수정시갱신_shouldUpdateLinks")
    void links_수정시갱신_shouldUpdateLinks() {
        // Given
        Long userId = 1L;
        String tenantId = "tenant-1";
        NoteResponse target = noteService.create(userId, new NoteCreateRequest(tenantId, "Java", "Content"));
        NoteResponse source = noteService.create(userId, new NoteCreateRequest(tenantId, "Source", "[[Java]]"));

        // When: 링크 변경 (Java -> Kotlin)
        noteService.update(userId, source.id(), new NoteCreateRequest(tenantId, "Source", "[[Kotlin]]"));

        // Then
        List<NoteResponse> javaBacklinks = noteService.getBacklinks(userId, target.id());
        assertThat(javaBacklinks).isEmpty();
    }
}
