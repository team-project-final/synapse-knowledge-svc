package com.synapse.knowledge.note.service;

import com.synapse.knowledge.global.exception.AccessDeniedException;
import com.synapse.knowledge.global.util.MarkdownSanitizer;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.dto.NoteVersionDetailResponse;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.entity.NoteLink;
import com.synapse.knowledge.note.kafka.outbox.NoteEventOutboxService;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteLinkRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.support.WikiLinkParser;
import com.synapse.knowledge.shared.NoteChunkingRequested;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteIdentityMapRepository noteIdentityMapRepository;
    private final NoteLinkRepository noteLinkRepository;
    private final WikiLinkParser linkParser;
    private final MarkdownSanitizer sanitizer;
    private final NoteEventOutboxService noteEventOutboxService;
    private final ApplicationEventPublisher eventPublisher;
    private final NoteVersionService noteVersionService;

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse create(Long userId, NoteCreateRequest request) {
        return create(userId, String.valueOf(userId), request);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse create(Long userId, String eventUserId, NoteCreateRequest request) {
        String sanitizedMd = sanitizer.sanitize(request.contentMd());
        String plainText = extractPlainText(sanitizedMd);

        Note note = Note.create(request.tenantId(), userId, request.title(), sanitizedMd, plainText, request.tags());
        Note savedNote = noteRepository.save(note);
        NoteIdentityMap identityMap = getOrCreateIdentityMap(savedNote.getId());

        updateWikiLinks(savedNote.getId(), request.tenantId(), sanitizedMd);
        noteEventOutboxService.enqueueCreated(savedNote, identityMap.getExternalNoteId(), resolveEventUserId(eventUserId, userId));
        publishChunkingRequested(savedNote, resolveEventUserId(eventUserId, userId), "created");
        publishSearchSyncRequested(savedNote, identityMap.getExternalNoteId(), false);
        return NoteResponse.from(savedNote);
    }

    public Page<NoteResponse> findAll(Long userId, Pageable pageable) {
        return noteRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
            .map(NoteResponse::from);
    }

    public Page<NoteResponse> findAllByTag(Long userId, String tag, Pageable pageable) {
        return noteRepository.findByUserIdAndTagAndDeletedAtIsNull(userId, tag, pageable)
            .map(NoteResponse::from);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse restoreVersion(Long userId, Long noteId, Integer versionNo) {
        NoteVersionDetailResponse target = noteVersionService.getVersion(userId, noteId, versionNo);
        Note note = findValidNote(noteId);
        return update(userId, noteId,
            new NoteCreateRequest(note.getTenantId(), target.title(), target.contentMd(), note.getTags()));
    }

    public NoteResponse getById(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        return NoteResponse.from(note);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse update(Long userId, Long noteId, NoteCreateRequest request) {
        return update(userId, String.valueOf(userId), noteId, request);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public NoteResponse update(Long userId, String eventUserId, Long noteId, NoteCreateRequest request) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);

        noteVersionService.saveVersion(note);

        String sanitizedMd = sanitizer.sanitize(request.contentMd());
        String plainText = extractPlainText(sanitizedMd);

        note.update(request.title(), sanitizedMd, plainText, request.tags());
        NoteIdentityMap identityMap = getOrCreateIdentityMap(note.getId());

        updateWikiLinks(note.getId(), note.getTenantId(), sanitizedMd);
        noteEventOutboxService.enqueueUpdated(note, identityMap.getExternalNoteId(), resolveEventUserId(eventUserId, userId));
        publishChunkingRequested(note, resolveEventUserId(eventUserId, userId), "updated");
        publishSearchSyncRequested(note, identityMap.getExternalNoteId(), false);
        return NoteResponse.from(note);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);
        NoteIdentityMap identityMap = getOrCreateIdentityMap(note.getId());
        note.softDelete();
        eventPublisher.publishEvent(
            new NoteChunkingRequested(
                note.getId(),
                note.getTenantId(),
                String.valueOf(userId),
                null,
                "deleted",
                Instant.now()
            )
        );
        publishSearchSyncRequested(note, identityMap.getExternalNoteId(), true);
    }

    public List<NoteResponse> getOutlinks(Long userId, Long noteId) {
        Note note = findValidNote(noteId);
        validateOwner(userId, note);

        return noteLinkRepository.findBySourceNoteId(noteId).stream()
            .filter(link -> link.getTargetNoteId() != null)
            .map(link -> noteRepository.findById(link.getTargetNoteId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(NoteResponse::from)
            .toList();
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
        if (htmlOrMd == null) {
            return null;
        }
        return htmlOrMd.replaceAll("<[^>]*>", "").replaceAll("\\[|\\]|\\(|\\)|#|\\*|`", "").trim();
    }

    private void publishChunkingRequested(Note note, String reason) {
        publishChunkingRequested(note, String.valueOf(note.getUserId()), reason);
    }

    private void publishChunkingRequested(Note note, String actorId, String reason) {
        eventPublisher.publishEvent(
            new NoteChunkingRequested(
                note.getId(),
                note.getTenantId(),
                actorId,
                note.getContentPlain(),
                reason,
                Instant.now()
            )
        );
    }

    private NoteIdentityMap getOrCreateIdentityMap(Long noteId) {
        return noteIdentityMapRepository.findById(noteId)
            .orElseGet(() -> noteIdentityMapRepository.save(NoteIdentityMap.create(noteId)));
    }

    private String resolveEventUserId(String eventUserId, Long userId) {
        if (eventUserId != null && !eventUserId.isBlank()) {
            return eventUserId;
        }
        return String.valueOf(userId);
    }

    private void publishSearchSyncRequested(Note note, UUID externalNoteId, boolean deleted) {
        eventPublisher.publishEvent(
            new NoteSearchSyncRequested(
                note.getId(),
                externalNoteId,
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
