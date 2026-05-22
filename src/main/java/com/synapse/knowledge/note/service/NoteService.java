package com.synapse.knowledge.note.service;

import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteLink;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.repository.NoteLinkRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.support.WikiLinkParser;
import com.synapse.knowledge.shared.AccessDeniedException;
import com.synapse.knowledge.shared.MarkdownSanitizer;
import com.synapse.knowledge.shared.NoteChunkingRequested;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final WikiLinkParser linkParser;
    private final MarkdownSanitizer sanitizer;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse create(Long userId, NoteCreateRequest request) {
        String sanitizedMd = sanitizer.sanitize(request.contentMd());
        String plainText = extractPlainText(sanitizedMd);
        
        Note note = Note.create(request.tenantId(), userId, request.title(), sanitizedMd, plainText, request.tags());
        Note savedNote = noteRepository.save(note);
        
        updateWikiLinks(savedNote.getId(), request.tenantId(), sanitizedMd);
        publishChunkingRequested(savedNote, "created");
        publishSearchSyncRequested(savedNote, false);
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
        
        note.update(request.title(), sanitizedMd, plainText, request.tags());
        
        updateWikiLinks(note.getId(), note.getTenantId(), sanitizedMd);
        publishChunkingRequested(note, "updated");
        publishSearchSyncRequested(note, false);
        return NoteResponse.from(note);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        note.softDelete();
        eventPublisher.publishEvent(
            new NoteChunkingRequested(
                note.getId(),
                note.getTenantId(),
                null,
                "deleted",
                Instant.now()
            )
        );
        publishSearchSyncRequested(note, true);
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

    private void publishChunkingRequested(Note note, String reason) {
        eventPublisher.publishEvent(
            new NoteChunkingRequested(
                note.getId(),
                note.getTenantId(),
                note.getContentPlain(),
                reason,
                Instant.now()
            )
        );
    }

    private void publishSearchSyncRequested(Note note, boolean deleted) {
        eventPublisher.publishEvent(
            new NoteSearchSyncRequested(
                note.getId(),
                note.getTenantId(),
                note.getUserId(),
                note.getTitle(),
                note.getContentPlain(),
                note.getTags(),
                deleted,
                Instant.now()
            )
        );
    }
}
