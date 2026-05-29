package com.synapse.knowledge.chunking.service;

import com.synapse.knowledge.chunking.entity.NoteChunk;
import com.synapse.knowledge.chunking.repository.NoteChunkRepository;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.NoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class ChunkingIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteChunkRepository noteChunkRepository;

    @Autowired
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @BeforeEach
    void setUp() {
        noteChunkRepository.deleteAll();
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        noteChunkRepository.deleteAll();
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @DisplayName("create_노트저장후_should비동기청크가저장됨")
    @Test
    void create_노트저장후_should비동기청크가저장됨() {
        // Given
        Long userId = 100L;
        String longContent = buildLongContent("create", 620);

        // When
        NoteResponse response = noteService.create(userId, new NoteCreateRequest("tenant1", "Chunk Title", longContent));

        // Then
        List<NoteChunk> chunks = waitForChunks(response.id(), 2);
        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks).allMatch(chunk -> chunk.getTokenCount() > 0);
    }

    @DisplayName("update_노트수정후_should기존청크가삭제되고재생성됨")
    @Test
    void update_노트수정후_should기존청크가삭제되고재생성됨() {
        // Given
        Long userId = 100L;
        NoteResponse created = noteService.create(
            userId,
            new NoteCreateRequest("tenant1", "Chunk Title", buildLongContent("before", 620))
        );
        List<NoteChunk> initialChunks = waitForChunks(created.id(), 2);

        // When
        noteService.update(
            userId,
            created.id(),
            new NoteCreateRequest("tenant1", "Chunk Title Updated", buildLongContent("after", 120))
        );

        // Then
        List<NoteChunk> updatedChunks = waitForChunkPrefix(created.id(), "after");
        assertThat(updatedChunks).isNotEmpty();
        assertThat(updatedChunks).allMatch(chunk -> chunk.getChunkText().contains("after"));
        assertThat(updatedChunks).hasSizeLessThan(initialChunks.size());
    }

    @DisplayName("delete_노트삭제후_should관련청크가비동기로정리됨")
    @Test
    void delete_노트삭제후_should관련청크가비동기로정리됨() {
        // Given
        Long userId = 100L;
        NoteResponse created = noteService.create(
            userId,
            new NoteCreateRequest("tenant1", "Chunk Delete Title", buildLongContent("delete", 620))
        );
        waitForChunks(created.id(), 2);

        // When
        noteService.delete(userId, created.id());

        // Then
        List<NoteChunk> deletedChunks = waitUntil(created.id(), List::isEmpty);
        assertThat(deletedChunks).isEmpty();
    }

    private List<NoteChunk> waitForChunks(Long noteId, int minimumSize) {
        return waitUntil(noteId, chunks -> chunks.size() >= minimumSize);
    }

    private List<NoteChunk> waitForChunkPrefix(Long noteId, String expectedPrefix) {
        return waitUntil(noteId, chunks -> !chunks.isEmpty() && chunks.stream()
            .allMatch(chunk -> chunk.getChunkText().contains(expectedPrefix)));
    }

    private List<NoteChunk> waitUntil(Long noteId, java.util.function.Predicate<List<NoteChunk>> condition) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));

        while (Instant.now().isBefore(deadline)) {
            List<NoteChunk> chunks = noteChunkRepository.findByNoteIdOrderByChunkIndex(noteId);
            if (condition.test(chunks)) {
                return chunks;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Async chunking wait interrupted", ex);
            }
        }

        return noteChunkRepository.findByNoteIdOrderByChunkIndex(noteId);
    }

    private String buildLongContent(String prefix, int tokenCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tokenCount; i++) {
            builder.append(prefix).append(i).append(' ');
            if ((i + 1) % 80 == 0) {
                builder.append("\n\n");
            }
        }
        return builder.toString().trim();
    }
}
