package com.synapse.knowledge.chunking.application;

import com.synapse.knowledge.chunking.dto.ChunkMapper;
import com.synapse.knowledge.chunking.dto.ChunkResponse;
import com.synapse.knowledge.chunking.domain.NoteChunk;
import com.synapse.knowledge.chunking.domain.NoteChunkRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChunkingService {
    private static final int MAX_TOKENS = 512;
    private static final int OVERLAP_TOKENS = 50;

    private final NoteChunkRepository noteChunkRepository;
    private final TokenCounter tokenCounter;
    private final ChunkMapper chunkMapper;

    public List<ChunkResponse> getNoteChunksByNoteId(Long noteId) {
        return chunkMapper.toResponses(noteChunkRepository.findByNoteIdOrderByChunkIndex(noteId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNoteChunksByNoteId(Long noteId) {
        noteChunkRepository.deleteByNoteId(noteId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void chunkNote(Long noteId, String tenantId, String contentPlain) {
        deleteNoteChunksByNoteId(noteId);

        if (contentPlain == null || contentPlain.isBlank()) {
            return;
        }

        List<Segment> segments = splitIntoSegments(contentPlain);
        List<ChunkDraft> chunks = buildChunkDrafts(segments);
        persistChunks(noteId, chunks);
    }

    private List<Segment> splitIntoSegments(String contentPlain) {
        String[] rawParagraphs = contentPlain.split("(\\r?\\n){2,}");
        List<Segment> segments = new ArrayList<>();

        for (String rawParagraph : rawParagraphs) {
            String normalized = normalizeParagraph(rawParagraph);
            if (normalized.isBlank()) {
                continue;
            }

            List<String> paragraphTokens = tokenCounter.tokenize(normalized);
            if (paragraphTokens.size() <= MAX_TOKENS) {
                segments.add(new Segment(normalized, paragraphTokens));
                continue;
            }

            segments.addAll(splitLongParagraph(paragraphTokens));
        }

        return segments;
    }

    private List<Segment> splitLongParagraph(List<String> paragraphTokens) {
        List<Segment> segments = new ArrayList<>();
        int segmentSize = Math.max(1, MAX_TOKENS - OVERLAP_TOKENS);
        int start = 0;

        while (start < paragraphTokens.size()) {
            int end = Math.min(start + segmentSize, paragraphTokens.size());
            List<String> window = List.copyOf(paragraphTokens.subList(start, end));
            segments.add(new Segment(String.join(" ", window), window));

            start = end;
        }

        return segments;
    }

    private List<ChunkDraft> buildChunkDrafts(List<Segment> segments) {
        List<ChunkDraft> result = new ArrayList<>();
        List<String> currentTokens = new ArrayList<>();
        List<String> currentParts = new ArrayList<>();

        for (Segment segment : segments) {
            if (currentTokens.isEmpty()) {
                currentTokens.addAll(segment.tokens());
                currentParts.add(segment.text());
                continue;
            }

            if (currentTokens.size() + segment.tokens().size() <= MAX_TOKENS) {
                currentTokens.addAll(segment.tokens());
                currentParts.add(segment.text());
                continue;
            }

            result.add(new ChunkDraft(joinParts(currentParts), currentTokens.size()));

            int overlapCount = Math.min(OVERLAP_TOKENS, Math.max(0, MAX_TOKENS - segment.tokens().size()));
            List<String> overlapTokens = tail(currentTokens, overlapCount);

            currentTokens = new ArrayList<>(overlapTokens);
            currentParts = new ArrayList<>();
            if (!overlapTokens.isEmpty()) {
                currentParts.add(String.join(" ", overlapTokens));
            }

            currentTokens.addAll(segment.tokens());
            currentParts.add(segment.text());
        }

        if (!currentTokens.isEmpty()) {
            result.add(new ChunkDraft(joinParts(currentParts), currentTokens.size()));
        }

        return result;
    }

    private void persistChunks(Long noteId, List<ChunkDraft> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            ChunkDraft chunk = chunks.get(i);
            noteChunkRepository.save(
                NoteChunk.create(noteId, i, chunk.text(), chunk.tokenCount())
            );
        }
    }

    private List<String> tail(List<String> tokens, int overlapCount) {
        if (overlapCount == 0 || tokens.isEmpty()) {
            return List.of();
        }

        int start = Math.max(tokens.size() - overlapCount, 0);
        return List.copyOf(tokens.subList(start, tokens.size()));
    }

    private String normalizeParagraph(String rawParagraph) {
        return rawParagraph.replaceAll("\\s+", " ").trim();
    }

    private String joinParts(List<String> parts) {
        return parts.stream()
            .filter(part -> !part.isBlank())
            .reduce((left, right) -> left + "\n\n" + right)
            .orElse("");
    }

    private record Segment(String text, List<String> tokens) {
    }

    private record ChunkDraft(String text, int tokenCount) {
    }
}
