package com.synapse.knowledge.chunking.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class NoteChunkEmbeddingJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("updateEmbedding_정상업데이트_shouldvector리터럴로저장")
    void updateEmbedding_정상업데이트_shouldvector리터럴로저장() {
        NoteChunkEmbeddingJdbcRepository repository = new NoteChunkEmbeddingJdbcRepository(jdbcTemplate);
        when(jdbcTemplate.update(eq("""
        update note_chunks
        set embedding = cast(? as vector)
        where id = ?
        """), eq("[0.1, 0.2, 0.3]"), eq(10L))).thenReturn(1);

        repository.updateEmbedding(10L, List.of(0.1f, 0.2f, 0.3f));

        verify(jdbcTemplate).update(eq("""
        update note_chunks
        set embedding = cast(? as vector)
        where id = ?
        """), eq("[0.1, 0.2, 0.3]"), eq(10L));
    }

    @Test
    @DisplayName("updateEmbedding_대상행없음_should예외발생")
    void updateEmbedding_대상행없음_should예외발생() {
        NoteChunkEmbeddingJdbcRepository repository = new NoteChunkEmbeddingJdbcRepository(jdbcTemplate);
        when(jdbcTemplate.update(eq("""
        update note_chunks
        set embedding = cast(? as vector)
        where id = ?
        """), eq("[0.1]"), eq(10L))).thenReturn(0);

        assertThatThrownBy(() -> repository.updateEmbedding(10L, List.of(0.1f)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("note chunk embedding update 실패");
    }
}
