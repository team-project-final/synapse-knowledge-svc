package com.synapse.knowledge.chunking.repository;

import java.util.List;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoteChunkEmbeddingJdbcRepository {

    private static final String UPDATE_EMBEDDING_SQL = """
        update note_chunks
        set embedding = cast(? as vector)
        where id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public void updateEmbedding(Long noteChunkId, List<Float> embedding) {
        int updated = jdbcTemplate.update(UPDATE_EMBEDDING_SQL, toVectorLiteral(embedding), noteChunkId);
        if (updated != 1) {
            throw new IllegalStateException("note chunk embedding update 실패: chunkId=" + noteChunkId);
        }
    }

    private String toVectorLiteral(List<Float> embedding) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Float value : embedding) {
            joiner.add(Float.toString(value));
        }
        return joiner.toString();
    }
}
