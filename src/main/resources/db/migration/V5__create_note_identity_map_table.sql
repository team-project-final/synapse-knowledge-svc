CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE note_identity_map (
    note_id BIGINT PRIMARY KEY,
    external_note_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_identity_map_note_id
        FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

CREATE INDEX idx_note_identity_map_external_note_id
    ON note_identity_map (external_note_id);

INSERT INTO note_identity_map (note_id, external_note_id, created_at)
SELECT id, gen_random_uuid(), CURRENT_TIMESTAMP
FROM notes
WHERE NOT EXISTS (
    SELECT 1
    FROM note_identity_map
    WHERE note_identity_map.note_id = notes.id
);
