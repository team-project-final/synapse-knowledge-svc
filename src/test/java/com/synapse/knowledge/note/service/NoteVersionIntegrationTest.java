package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteVersionDetailResponse;
import com.synapse.knowledge.note.dto.NoteVersionSummaryResponse;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.repository.NoteVersionRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoteVersionIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteVersionService noteVersionService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteVersionRepository noteVersionRepository;

    @Autowired
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Test
    @DisplayName("노트 수정 시 이전 버전이 자동으로 저장된다")
    void update_onNoteModification_shouldAutoSavePreviousVersion() {
        Long userId = 1L;
        Long noteId = noteService.create(userId, new NoteCreateRequest("tenant1", "초기 제목", "초기 내용")).id();

        noteService.update(userId, noteId, new NoteCreateRequest("tenant1", "수정 제목", "수정 내용"));

        List<NoteVersionSummaryResponse> versions = noteVersionService.listVersions(userId, noteId);
        assertThat(versions).hasSize(1);
        assertThat(versions.get(0).title()).isEqualTo("초기 제목");
        assertThat(versions.get(0).versionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("수정을 여러 번 하면 버전 번호가 순차 증가한다")
    void update_multipleUpdates_shouldIncrementVersionNumbers() {
        Long userId = 1L;
        Long noteId = noteService.create(userId, new NoteCreateRequest("tenant1", "v0", "초기")).id();

        noteService.update(userId, noteId, new NoteCreateRequest("tenant1", "v1", "첫 수정"));
        noteService.update(userId, noteId, new NoteCreateRequest("tenant1", "v2", "두번째 수정"));

        List<NoteVersionSummaryResponse> versions = noteVersionService.listVersions(userId, noteId);
        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).versionNo()).isEqualTo(2);
        assertThat(versions.get(1).versionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("버전 상세 조회 시 해당 버전의 제목과 내용을 반환한다")
    void getVersion_validVersionNo_shouldReturnVersionDetail() {
        Long userId = 1L;
        Long noteId = noteService.create(userId, new NoteCreateRequest("tenant1", "원본 제목", "원본 내용")).id();
        noteService.update(userId, noteId, new NoteCreateRequest("tenant1", "수정 제목", "수정 내용"));

        NoteVersionDetailResponse detail = noteVersionService.getVersion(userId, noteId, 1);

        assertThat(detail.versionNo()).isEqualTo(1);
        assertThat(detail.title()).isEqualTo("원본 제목");
        assertThat(detail.contentMd()).isEqualTo("원본 내용");
    }

    @Test
    @DisplayName("버전 복원 시 현재 상태를 새 버전으로 저장하고 이벤트 파이프라인을 통해 노트 내용을 복원한다")
    void restoreVersion_validVersion_shouldSaveSnapshotAndRestoreNoteViaUpdate() {
        Long userId = 1L;
        Long noteId = noteService.create(userId, new NoteCreateRequest("tenant1", "원본 제목", "원본 내용")).id();
        noteService.update(userId, noteId, new NoteCreateRequest("tenant1", "수정 제목", "수정 내용"));

        NoteResponse restored = noteService.restoreVersion(userId, noteId, 1);

        assertThat(restored.title()).isEqualTo("원본 제목");
        assertThat(noteVersionRepository.countByNoteId(noteId)).isEqualTo(2);
        assertThat(noteRepository.findByIdAndDeletedAtIsNull(noteId).get().getTitle()).isEqualTo("원본 제목");
    }

    @Test
    @DisplayName("다른 사용자의 버전 목록 조회 시 AccessDeniedException을 던진다")
    void listVersions_anotherUsersNote_shouldThrowAccessDeniedException() {
        Long ownerId = 1L;
        Long otherId = 2L;
        Long noteId = noteService.create(ownerId, new NoteCreateRequest("tenant1", "제목", "내용")).id();
        noteService.update(ownerId, noteId, new NoteCreateRequest("tenant1", "수정 제목", "수정 내용"));

        assertThatThrownBy(() -> noteVersionService.listVersions(otherId, noteId))
            .isInstanceOf(AccessDeniedException.class);
    }
}
