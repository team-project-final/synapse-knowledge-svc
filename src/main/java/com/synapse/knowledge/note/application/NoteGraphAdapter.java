package com.synapse.knowledge.note.application;

import com.synapse.knowledge.note.domain.NoteLinkRepository;
import com.synapse.knowledge.note.domain.NoteRepository;
import com.synapse.knowledge.shared.GraphLinkData;
import com.synapse.knowledge.shared.GraphNoteData;
import com.synapse.knowledge.shared.GraphQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteGraphAdapter implements GraphQueryPort {

    private final NoteRepository noteRepository;
    private final NoteLinkRepository noteLinkRepository;

    @Override
    public List<GraphNoteData> findAllNoteByUserId(Long userId) {
        return noteRepository.findTop1000ByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(note -> new GraphNoteData(note.getId(), note.getTitle(), note.getUserId()))
                .toList();
    }

    @Override
    public List<GraphLinkData> findAllLinksByUserId(Long userId) {
        List<Long> noteIds = noteRepository.findTop1000ByUserIdAndDeletedAtIsNull(userId)
                .stream()
                .map(note -> note.getId())
                .toList();

        return noteLinkRepository.findBySourceNoteIdIn(noteIds)
                .stream()
                .filter(link -> link.getTargetNoteId() != null)
                .map(link -> new GraphLinkData(link.getSourceNoteId(), link.getTargetNoteId()))
                .toList();
    }

    @Override
    public List<GraphLinkData> findLinksByNoteId(Long noteId) {
        List<GraphLinkData> outLinks = noteLinkRepository.findBySourceNoteId(noteId)
                .stream()
                .filter(link -> link.getTargetNoteId() != null)
                .map(link -> new GraphLinkData(link.getSourceNoteId(), link.getTargetNoteId()))
                .toList();

        List<GraphLinkData> inLinks = noteLinkRepository.findByTargetNoteId(noteId)
                .stream()
                .filter(link -> link.getSourceNoteId() != null)
                .map(link -> new GraphLinkData(link.getSourceNoteId(), link.getTargetNoteId()))
                .toList();

        return java.util.stream.Stream.concat(outLinks.stream(), inLinks.stream()).toList();
    }

    @Override
    public List<GraphNoteData> findNotesByIds(List<Long> noteIds) {
        return noteRepository.findAllById(noteIds)
                .stream()
                .filter(note -> note.getDeletedAt() == null)
                .map(note -> new GraphNoteData(note.getId(), note.getTitle(), note.getUserId()))
                .toList();
    }

    @Override
    public List<GraphLinkData> findNeighborLinksByDepth(Long noteId, int maxDepth) {
        return noteLinkRepository.findNeighborLinksByDepthNative(noteId, maxDepth)
                .stream()
                .map(row -> new GraphLinkData(((Number) row[0]).longValue(), ((Number) row[1]).longValue()))
                .toList();
    }
}
