CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE note_chunks (
    id BIGSERIAL PRIMARY KEY,
    note_id BIGINT NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    token_count INTEGER NOT NULL,
    embedding vector(1536),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_chunks_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

CREATE INDEX idx_note_chunks_note_id ON note_chunks(note_id);
CREATE UNIQUE INDEX uk_note_chunks_note_id_chunk_index ON note_chunks(note_id, chunk_index);
