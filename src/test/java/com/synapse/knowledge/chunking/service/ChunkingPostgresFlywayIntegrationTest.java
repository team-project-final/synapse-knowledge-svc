package com.synapse.knowledge.chunking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.chunking.entity.NoteChunk;
import com.synapse.knowledge.chunking.repository.NoteChunkRepository;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.NoteService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.kafka.listener.auto-startup=false",
    "synapse.kafka.enabled=false"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ChunkingPostgresFlywayIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("synapse")
            .withUsername("synapse")
            .withPassword("synapse");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @DisplayName("flyway_실행후_should_pgvector확장과_noteChunks스키마가생성됨")
    @Test
    void flyway_실행후_should_pgvector확장과_noteChunks스키마가생성됨() {
        // Given, When
        Boolean vectorExtensionExists = jdbcTemplate.queryForObject(
            "select exists(select 1 from pg_extension where extname = 'vector')",
            Boolean.class
        );
        String embeddingType = jdbcTemplate.queryForObject(
            """
            select udt_name
            from information_schema.columns
            where table_name = 'note_chunks'
              and column_name = 'embedding'
            """,
            String.class
        );
        String deleteRule = jdbcTemplate.queryForObject(
            """
            select c.confdeltype
            from pg_constraint c
            join pg_class t on t.oid = c.conrelid
            where t.relname = 'note_chunks'
              and c.conname = 'fk_note_chunks_note'
            """,
            String.class
        );
        List<String> indexes = jdbcTemplate.queryForList(
            """
            select indexname
            from pg_indexes
            where tablename = 'note_chunks'
            order by indexname
            """,
            String.class
        );

        // Then
        assertThat(vectorExtensionExists).isTrue();
        assertThat(embeddingType).isEqualTo("vector");
        assertThat(deleteRule).isEqualTo("c");
        assertThat(indexes).contains(
            "idx_note_chunks_note_id",
            "uk_note_chunks_note_id_chunk_index"
        );
    }

    @DisplayName("chunking_실제postgres스키마에서_should_createUpdateDelete가동작함")
    @Test
    void chunking_실제postgres스키마에서_should_createUpdateDelete가동작함() {
        // Given
        Long userId = 100L;
        NoteResponse created = noteService.create(
            userId,
            new NoteCreateRequest("tenant1", "Chunk Title", buildLongContent("before", 620))
        );

        // When
        List<NoteChunk> initialChunks = waitForChunks(created.id(), 2);
        noteService.update(
            userId,
            created.id(),
            new NoteCreateRequest("tenant1", "Chunk Title Updated", buildLongContent("after", 120))
        );
        List<NoteChunk> updatedChunks = waitForChunkPrefix(created.id(), "after");
        noteService.delete(userId, created.id());
        List<NoteChunk> deletedChunks = waitUntil(created.id(), List::isEmpty);

        // Then
        assertThat(initialChunks).hasSizeGreaterThan(1);
        assertThat(updatedChunks).isNotEmpty();
        assertThat(updatedChunks).allMatch(chunk -> chunk.getChunkText().contains("after"));
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
