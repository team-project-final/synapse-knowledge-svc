CREATE TABLE note_links (
    id BIGSERIAL PRIMARY KEY,
    source_note_id BIGINT NOT NULL,
    target_note_id BIGINT,
    target_title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_source_note FOREIGN KEY (source_note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_target_note FOREIGN KEY (target_note_id) REFERENCES notes(id) ON DELETE SET NULL
);

CREATE INDEX idx_note_links_source ON note_links(source_note_id);
CREATE INDEX idx_note_links_target_id ON note_links(target_note_id);
CREATE INDEX idx_note_links_target_title ON note_links(target_title);
CREATE UNIQUE INDEX uk_note_links_source_target ON note_links(source_note_id, target_title);
