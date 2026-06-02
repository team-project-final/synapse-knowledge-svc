CREATE TABLE IF NOT EXISTS note_tags (
    note_id BIGINT NOT NULL,
    tag VARCHAR(30) NOT NULL,
    CONSTRAINT fk_note_tags_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_note_tags_note_id ON note_tags(note_id);
CREATE INDEX IF NOT EXISTS idx_note_tags_tag ON note_tags(tag);
