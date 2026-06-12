package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.client.SharedContentValidationClient;
import com.synapse.knowledge.note.client.SharedContentValidationResponse;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteSharedAccessIntegrationTest {

    private static final Long OWNER_ID = 100L;
    private static final Long COPIER_ID = 200L;
    private static final UUID SHARED_CONTENT_ID = UUID.randomUUID();
    private static final String SHARE_TOKEN = "share-token-abc";
    private static final String AUTH = "Bearer test-jwt";

    @Autowired
    private NoteService noteService;

    @MockitoBean
    private SharedContentValidationClient validationClient;

    private void mockValidation(boolean valid, String ownerId, String reason) {
        when(validationClient.validate(anyString(), any(), anyString(), anyLong()))
            .thenReturn(new SharedContentValidationResponse(
                valid, SHARED_CONTENT_ID.toString(), "NOTE", "1", ownerId, reason));
    }

    @Test
    void getSharedDetail_검증성공_소유자일치_shouldReturnNote() {
        NoteResponse created = noteService.create(OWNER_ID, new NoteCreateRequest("tenant1", "공유 제목", "공유 본문"));
        mockValidation(true, String.valueOf(OWNER_ID), null);

        NoteResponse result = noteService.getSharedDetail(AUTH, created.id(), SHARED_CONTENT_ID, SHARE_TOKEN);

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.title()).isEqualTo("공유 제목");
    }

    @Test
    void getSharedDetail_검증실패_shouldThrowAccessDenied() {
        NoteResponse created = noteService.create(OWNER_ID, new NoteCreateRequest("tenant1", "t", "c"));
        mockValidation(false, null, "TOKEN_MISMATCH");

        assertThatThrownBy(() -> noteService.getSharedDetail(AUTH, created.id(), SHARED_CONTENT_ID, SHARE_TOKEN))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getSharedDetail_소유자불일치_shouldThrowAccessDenied() {
        NoteResponse created = noteService.create(OWNER_ID, new NoteCreateRequest("tenant1", "t", "c"));
        mockValidation(true, "999", null); // ownerId=999 → resolve(999) != 100

        assertThatThrownBy(() -> noteService.getSharedDetail(AUTH, created.id(), SHARED_CONTENT_ID, SHARE_TOKEN))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getSharedDetail_없는노트_shouldThrowAccessDenied() {
        assertThatThrownBy(() -> noteService.getSharedDetail(AUTH, 999999L, SHARED_CONTENT_ID, SHARE_TOKEN))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void copyFromShare_검증성공_shouldCreateCopyForCurrentUser() {
        NoteResponse origin = noteService.create(
            OWNER_ID, new NoteCreateRequest("tenant1", "원본", "원본 본문", List.of("a", "b")));
        mockValidation(true, String.valueOf(OWNER_ID), null);

        NoteResponse copy = noteService.copyFromShare(AUTH, COPIER_ID, origin.id(), SHARED_CONTENT_ID, SHARE_TOKEN);

        assertThat(copy.id()).isNotEqualTo(origin.id());
        assertThat(copy.title()).isEqualTo("원본");
        assertThat(copy.tags()).containsExactly("a", "b");
        // 복사본은 복사자(COPIER_ID) 소유 → 복사자로 조회 가능해야 한다
        assertThat(noteService.getById(COPIER_ID, copy.id()).id()).isEqualTo(copy.id());
    }

    @Test
    void copyFromShare_검증실패_shouldThrowAccessDenied() {
        NoteResponse origin = noteService.create(OWNER_ID, new NoteCreateRequest("tenant1", "원본", "본문"));
        mockValidation(false, null, "CONTENT_ID_MISMATCH");

        assertThatThrownBy(() -> noteService.copyFromShare(AUTH, COPIER_ID, origin.id(), SHARED_CONTENT_ID, SHARE_TOKEN))
            .isInstanceOf(AccessDeniedException.class);
    }
}
