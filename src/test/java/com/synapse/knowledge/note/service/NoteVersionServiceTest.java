package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.dto.NoteVersionDetailResponse;
import com.synapse.knowledge.note.dto.NoteVersionSummaryResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteVersion;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.repository.NoteVersionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteVersionServiceTest {

    @Mock
    private NoteVersionRepository noteVersionRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteVersionService noteVersionService;

    @Test
    @DisplayName("노트 수정 시 현재 상태를 새 버전으로 저장한다")
    void saveVersion_onNoteUpdate_shouldPersistCurrentStateAsNewVersion() {
        Note note = Note.create("tenant1", 1L, "제목", "내용", "내용", List.of());
        given(noteVersionRepository.findMaxVersionNoByNoteId(any())).willReturn(0);
        given(noteVersionRepository.countByNoteId(any())).willReturn(1);

        noteVersionService.saveVersion(note);

        verify(noteVersionRepository).save(any(NoteVersion.class));
    }

    @Test
    @DisplayName("버전이 50개 초과이면 가장 오래된 버전을 삭제한다")
    void saveVersion_over50Versions_shouldDeleteOldestVersion() {
        Note note = Note.create("tenant1", 1L, "제목", "내용", "내용", List.of());
        given(noteVersionRepository.findMaxVersionNoByNoteId(any())).willReturn(50);
        given(noteVersionRepository.countByNoteId(any())).willReturn(51);
        given(noteVersionRepository.findMinVersionNoByNoteId(any())).willReturn(1);

        noteVersionService.saveVersion(note);

        verify(noteVersionRepository).deleteByNoteIdAndVersionNo(any(), any());
    }

    @Test
    @DisplayName("버전 목록 조회 시 소유자가 아니면 AccessDeniedException을 던진다")
    void listVersions_notOwner_shouldThrowAccessDeniedException() {
        Note note = Note.create("tenant1", 99L, "제목", "내용", "내용", List.of());
        given(noteRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(note));

        assertThatThrownBy(() -> noteVersionService.listVersions(1L, 1L))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("버전 목록 조회 시 소유자이면 버전 목록을 반환한다")
    void listVersions_owner_shouldReturnVersionList() {
        Note note = Note.create("tenant1", 1L, "제목", "내용", "내용", List.of());
        NoteVersion v = NoteVersion.of(1L, 1, "제목", "내용");
        given(noteRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(note));
        given(noteVersionRepository.findByNoteIdOrderByVersionNoDesc(1L)).willReturn(List.of(v));

        List<NoteVersionSummaryResponse> result = noteVersionService.listVersions(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).versionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 버전 조회 시 RuntimeException을 던진다")
    void getVersion_notFound_shouldThrowRuntimeException() {
        Note note = Note.create("tenant1", 1L, "제목", "내용", "내용", List.of());
        given(noteRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(note));
        given(noteVersionRepository.findByNoteIdAndVersionNo(1L, 99)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noteVersionService.getVersion(1L, 1L, 99))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Note version not found");
    }

    @Test
    @DisplayName("saveVersion 호출 시 버전이 저장되고 50개 초과 시 삭제를 시도한다")
    void saveVersion_at51Versions_shouldAttemptDeleteOldest() {
        Note note = Note.create("tenant1", 1L, "현재 제목", "현재 내용", "현재 내용", List.of());
        given(noteVersionRepository.findMaxVersionNoByNoteId(any())).willReturn(50);
        given(noteVersionRepository.countByNoteId(any())).willReturn(51);
        given(noteVersionRepository.findMinVersionNoByNoteId(any())).willReturn(1);

        noteVersionService.saveVersion(note);

        verify(noteVersionRepository).save(any(NoteVersion.class));
        verify(noteVersionRepository).deleteByNoteIdAndVersionNo(any(), any());
    }
}
