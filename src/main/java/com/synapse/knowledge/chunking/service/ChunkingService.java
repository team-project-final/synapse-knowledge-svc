package com.synapse.knowledge.chunking.service;

import com.synapse.knowledge.chunking.client.LearningAiEmbeddingClient;
import com.synapse.knowledge.chunking.config.ChunkingAiProperties;
import com.synapse.knowledge.chunking.dto.ChunkMapper;
import com.synapse.knowledge.chunking.dto.ChunkResponse;
import com.synapse.knowledge.chunking.entity.NoteChunk;
import com.synapse.knowledge.chunking.repository.NoteChunkEmbeddingJdbcRepository;
import com.synapse.knowledge.chunking.repository.NoteChunkRepository;
import com.synapse.knowledge.chunking.service.support.TokenCounter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

@Service
@RequiredArgsConstructor
public class ChunkingService {
    private static final int MAX_TOKENS = 512;
    private static final int OVERLAP_TOKENS = 50;
    private static final String DEFAULT_ACTOR_ID = "knowledge-chunking";

    private final NoteChunkRepository noteChunkRepository;
    private final NoteChunkEmbeddingJdbcRepository noteChunkEmbeddingJdbcRepository;
    private final LearningAiEmbeddingClient learningAiEmbeddingClient;
    private final ChunkingAiProperties chunkingAiProperties;
    private final TokenCounter tokenCounter;
    private final ChunkMapper chunkMapper;
    private final TransactionOperations transactionOperations;

    @Transactional(readOnly = true)
    public List<ChunkResponse> getNoteChunksByNoteId(Long noteId) {
        return chunkMapper.toResponses(noteChunkRepository.findByNoteIdOrderByChunkIndex(noteId));
    }

    public void deleteNoteChunksByNoteId(Long noteId) {
        transactionOperations.executeWithoutResult(status -> noteChunkRepository.deleteByNoteId(noteId));
    }

    public void chunkNote(Long noteId, String actorId, String contentPlain) {
        if (contentPlain == null || contentPlain.isBlank()) {
            deleteNoteChunksByNoteId(noteId);
            return;
        }

        List<Segment> segments = splitIntoSegments(contentPlain);
        List<ChunkDraft> chunks = buildChunkDrafts(segments);
        if (chunks.isEmpty()) {
            deleteNoteChunksByNoteId(noteId);
            return;
        }

        List<List<Float>> embeddings = fetchEmbeddings(resolveActorId(actorId), chunks);
        replaceChunks(noteId, chunks, embeddings);
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

    private List<List<Float>> fetchEmbeddings(String actorId, List<ChunkDraft> chunks) {
        if (!chunkingAiProperties.enabled()) {
            return List.of();
        }

        LearningAiEmbeddingClient.EmbeddingBatchResponse response = learningAiEmbeddingClient.createEmbeddings(
            actorId,
            chunks.stream()
                .map(ChunkDraft::text)
                .toList()
        );
        validateEmbeddingCount(chunks.size(), response.embeddings().size());
        validateEmbeddingDimensions(response.embeddings());
        return response.embeddings();
    }

    private void replaceChunks(Long noteId, List<ChunkDraft> chunks, List<List<Float>> embeddings) {
        transactionOperations.executeWithoutResult(status -> {
            noteChunkRepository.deleteByNoteId(noteId);
            for (int i = 0; i < chunks.size(); i++) {
                ChunkDraft chunk = chunks.get(i);
                NoteChunk savedChunk = noteChunkRepository.save(
                    NoteChunk.create(noteId, i, chunk.text(), chunk.tokenCount())
                );
                if (chunkingAiProperties.enabled()) {
                    noteChunkEmbeddingJdbcRepository.updateEmbedding(savedChunk.getId(), embeddings.get(i));
                }
            }
        });
    }

    private void validateEmbeddingCount(int expectedChunkCount, int actualEmbeddingCount) {
        if (expectedChunkCount != actualEmbeddingCount) {
            throw new IllegalStateException(
                "임베딩 개수 불일치: expectedChunks=" + expectedChunkCount + ", actualEmbeddings=" + actualEmbeddingCount
            );
        }
    }

    private void validateEmbeddingDimensions(List<List<Float>> embeddings) {
        for (int i = 0; i < embeddings.size(); i++) {
            List<Float> embedding = embeddings.get(i);
            if (embedding == null || embedding.size() != chunkingAiProperties.expectedDimensions()) {
                throw new IllegalStateException(
                    "임베딩 차원 불일치: chunkIndex=" + i
                        + ", expectedDimensions=" + chunkingAiProperties.expectedDimensions()
                        + ", actualDimensions=" + (embedding == null ? 0 : embedding.size())
                );
            }
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

    private String resolveActorId(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return DEFAULT_ACTOR_ID;
        }
        return actorId;
    }

    private record Segment(String text, List<String> tokens) {
    }

    private record ChunkDraft(String text, int tokenCount) {
    }
}
