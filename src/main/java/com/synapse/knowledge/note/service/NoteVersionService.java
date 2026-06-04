package com.synapse.knowledge.note.service;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.note.dto.NoteVersionDetailResponse;
import com.synapse.knowledge.note.dto.NoteVersionSummaryResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteVersion;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.repository.NoteVersionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteVersionService {

    static final int MAX_VERSIONS = 50;

    private final NoteVersionRepository noteVersionRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public void saveVersion(Note note) {
        int nextVersionNo = noteVersionRepository.findMaxVersionNoByNoteId(note.getId()) + 1;
        noteVersionRepository.save(NoteVersion.of(note.getId(), nextVersionNo, note.getTitle(), note.getContentMd()));

        if (noteVersionRepository.countByNoteId(note.getId()) > MAX_VERSIONS) {
            int oldest = noteVersionRepository.findMinVersionNoByNoteId(note.getId());
            try {
                noteVersionRepository.deleteByNoteIdAndVersionNo(note.getId(), oldest);
            } catch (Exception e) {
                log.warn("버전 정리 실패 (noteId={}, versionNo={}): {}", note.getId(), oldest, e.getMessage());
            }
        }
    }

    public List<NoteVersionSummaryResponse> listVersions(Long userId, Long noteId) {
        validateOwner(userId, noteId);
        return noteVersionRepository.findByNoteIdOrderByVersionNoDesc(noteId).stream()
            .map(NoteVersionSummaryResponse::from)
            .toList();
    }

    public NoteVersionDetailResponse getVersion(Long userId, Long noteId, Integer versionNo) {
        validateOwner(userId, noteId);
        NoteVersion version = findVersion(noteId, versionNo);
        return NoteVersionDetailResponse.from(version);
    }

    private NoteVersion findVersion(Long noteId, Integer versionNo) {
        return noteVersionRepository.findByNoteIdAndVersionNo(noteId, versionNo)
            .orElseThrow(() -> new RuntimeException("Note version not found: noteId=" + noteId + ", versionNo=" + versionNo));
    }

    private Note findNoteAndValidateOwner(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndDeletedAtIsNull(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + noteId));
        validateOwner(userId, note);
        return note;
    }

    private void validateOwner(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndDeletedAtIsNull(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found: " + noteId));
        validateOwner(userId, note);
    }

    private void validateOwner(Long userId, Note note) {
        if (!note.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 노트에 접근 권한이 없습니다");
        }
    }
}
