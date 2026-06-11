package com.synapse.knowledge.chunking.entity;

import com.synapse.knowledge.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(
    name = "note_chunks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_note_chunks_note_id_chunk_index", columnNames = {"note_id", "chunk_index"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteChunk extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long noteId;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    @Column(nullable = false)
    private Integer tokenCount;

    @Column(insertable = false, updatable = false)
    @ColumnTransformer(read = "cast(embedding as varchar)")
    private String embedding;

    public static NoteChunk create(Long noteId, Integer chunkIndex, String chunkText, Integer tokenCount) {
        NoteChunk noteChunk = new NoteChunk();
        noteChunk.noteId = noteId;
        noteChunk.chunkIndex = chunkIndex;
        noteChunk.chunkText = chunkText;
        noteChunk.tokenCount = tokenCount;
        return noteChunk;
    }
}
