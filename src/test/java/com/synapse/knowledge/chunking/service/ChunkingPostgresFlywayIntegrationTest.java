package com.synapse.knowledge.chunking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.synapse.knowledge.chunking.client.LearningAiEmbeddingClient;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.kafka.listener.auto-startup=false",
    "synapse.kafka.enabled=false",
    "chunking.ai.enabled=true"
})
@ActiveProfiles("test")
class ChunkingPostgresFlywayIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> lookupProperty(
            "chunking.pg.jdbc-url",
            "CHUNKING_PG_JDBC_URL",
            "jdbc:postgresql://localhost:5432/synapse"
        ));
        registry.add("spring.datasource.username", () -> lookupProperty(
            "chunking.pg.username",
            "CHUNKING_PG_USERNAME",
            "synapse"
        ));
        registry.add("spring.datasource.password", () -> lookupProperty(
            "chunking.pg.password",
            "CHUNKING_PG_PASSWORD",
            "synapse_local_pw"
        ));
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.url", () -> lookupProperty(
            "chunking.pg.jdbc-url",
            "CHUNKING_PG_JDBC_URL",
            "jdbc:postgresql://localhost:5432/synapse"
        ));
        registry.add("spring.flyway.user", () -> lookupProperty(
            "chunking.pg.username",
            "CHUNKING_PG_USERNAME",
            "synapse"
        ));
        registry.add("spring.flyway.password", () -> lookupProperty(
            "chunking.pg.password",
            "CHUNKING_PG_PASSWORD",
            "synapse_local_pw"
        ));
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

    @MockitoBean
    private LearningAiEmbeddingClient learningAiEmbeddingClient;

    @BeforeEach
    void setUp() {
        noteChunkRepository.deleteAll();
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
        given(learningAiEmbeddingClient.createEmbeddings(anyString(), anyList()))
            .willAnswer(invocation -> {
                @SuppressWarnings("unchecked")
                List<String> texts = invocation.getArgument(1);
                List<List<Float>> embeddings = texts.stream()
                    .map(text -> createEmbedding(text.hashCode()))
                    .toList();
                return new LearningAiEmbeddingClient.EmbeddingBatchResponse(embeddings, "mock-model");
            });
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
        waitForEmbeddings(created.id(), initialChunks.size());

        // Then
        assertThat(initialChunks).hasSizeGreaterThan(1);
        assertThat(initialChunks).allMatch(chunk -> chunk.getEmbedding() != null && !chunk.getEmbedding().isBlank());
        assertEmbeddingStored(created.id(), initialChunks.size());

        // When
        noteService.update(
            userId,
            created.id(),
            new NoteCreateRequest("tenant1", "Chunk Title Updated", buildLongContent("after", 120))
        );
        List<NoteChunk> updatedChunks = waitForChunkPrefix(created.id(), "after");
        waitForEmbeddings(created.id(), updatedChunks.size());

        // Then
        assertThat(updatedChunks).isNotEmpty();
        assertThat(updatedChunks).allMatch(chunk -> chunk.getChunkText().contains("after"));
        assertThat(updatedChunks).allMatch(chunk -> chunk.getEmbedding() != null && !chunk.getEmbedding().isBlank());
        assertEmbeddingStored(created.id(), updatedChunks.size());

        // When
        noteService.delete(userId, created.id());
        List<NoteChunk> deletedChunks = waitUntil(created.id(), List::isEmpty);

        // Then
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

    private void waitForEmbeddings(Long noteId, int expectedCount) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));

        while (Instant.now().isBefore(deadline)) {
            Integer embeddedChunkCount = jdbcTemplate.queryForObject(
                """
                select count(*)
                from note_chunks
                where note_id = ?
                  and embedding is not null
                """,
                Integer.class,
                noteId
            );
            if (embeddedChunkCount != null && embeddedChunkCount == expectedCount) {
                return;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Async embedding wait interrupted", ex);
            }
        }
    }

    private void assertEmbeddingStored(Long noteId, int expectedChunkCount) {
        Integer embeddedChunkCount = jdbcTemplate.queryForObject(
            """
            select count(*)
            from note_chunks
            where note_id = ?
              and embedding is not null
            """,
            Integer.class,
            noteId
        );
        Integer minDimensions = jdbcTemplate.queryForObject(
            """
            select min(vector_dims(embedding))
            from note_chunks
            where note_id = ?
            """,
            Integer.class,
            noteId
        );

        assertThat(embeddedChunkCount).isEqualTo(expectedChunkCount);
        assertThat(minDimensions).isEqualTo(1536);
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

    private List<Float> createEmbedding(int seed) {
        float base = Math.abs(seed % 1000) / 1000.0f;
        List<Float> embedding = new java.util.ArrayList<>(1536);
        for (int i = 0; i < 1536; i++) {
            embedding.add(base + (i * 0.0001f));
        }
        return embedding;
    }

    private static String lookupProperty(String systemPropertyKey, String envKey, String defaultValue) {
        String systemPropertyValue = System.getProperty(systemPropertyKey);
        if (systemPropertyValue != null && !systemPropertyValue.isBlank()) {
            return systemPropertyValue;
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}
