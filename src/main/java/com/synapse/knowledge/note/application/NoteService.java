package com.synapse.knowledge.note.application;

import com.synapse.knowledge.note.domain.Note;
import com.synapse.knowledge.note.domain.NoteRepository;
import com.synapse.knowledge.note.domain.NoteLink;
import com.synapse.knowledge.note.domain.NoteLinkRepository;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.shared.AccessDeniedException;
import com.synapse.knowledge.shared.MarkdownSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final WikiLinkParser linkParser;
    private final MarkdownSanitizer sanitizer;

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse create(Long userId, NoteCreateRequest request) {
        String sanitizedMd = sanitizer.sanitize(request.contentMd());
        String plainText = extractPlainText(sanitizedMd);
        
        Note note = Note.create(request.tenantId(), userId, request.title(), sanitizedMd, plainText);
        Note savedNote = noteRepository.save(note);
        
        updateWikiLinks(savedNote.getId(), request.tenantId(), sanitizedMd);
        return NoteResponse.from(savedNote);
    }

    public Page<NoteResponse> findAll(Long userId, Pageable pageable) {
        return noteRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
            .map(NoteResponse::from);
    }

    public NoteResponse getById(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        return NoteResponse.from(note);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse update(Long userId, Long noteId, NoteCreateRequest request) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        
        String sanitizedMd = sanitizer.sanitize(request.contentMd());
        String plainText = extractPlainText(sanitizedMd);
        
        note.update(request.title(), sanitizedMd, plainText);
        
        updateWikiLinks(note.getId(), note.getTenantId(), sanitizedMd);
        return NoteResponse.from(note);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        note.softDelete();
    }

    public List<NoteResponse> getBacklinks(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);

        return noteLinkRepository.findByTargetNoteId(noteId).stream()
            .map(link -> noteRepository.findById(link.getSourceNoteId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(NoteResponse::from)
            .toList();
    }

    private void updateWikiLinks(Long sourceNoteId, String tenantId, String contentMd) {
        noteLinkRepository.deleteBySourceNoteId(sourceNoteId);
        
        Set<String> titles = linkParser.parse(contentMd);
        for (String title : titles) {
            Optional<Note> targetNote = noteRepository.findByTenantIdAndTitleAndDeletedAtIsNull(tenantId, title);
            NoteLink link = NoteLink.create(sourceNoteId, targetNote.map(Note::getId).orElse(null), title);
            noteLinkRepository.save(link);
        }
    }

    private Note findValidNote(Long noteId) {
        return noteRepository.findByIdAndDeletedAtIsNull(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    private void validateOwner(Long userId, Note note) {
        if (!note.getUserId().equals(userId)) {
            throw new AccessDeniedException("이 노트에 접근 권한이 없습니다");
        }
    }

    private String extractPlainText(String htmlOrMd) {
        if (htmlOrMd == null) return null;
        return htmlOrMd.replaceAll("<[^>]*>", "").replaceAll("\\[|\\]|\\(|\\)|#|\\*|`", "").trim();
    }
}
