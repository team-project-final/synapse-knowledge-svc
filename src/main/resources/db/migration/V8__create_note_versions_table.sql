CREATE TABLE note_versions (
    id         BIGSERIAL PRIMARY KEY,
    note_id    BIGINT       NOT NULL,
    version_no INT          NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content_md TEXT         NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_note_versions_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT uq_note_version UNIQUE (note_id, version_no)
);

CREATE INDEX idx_note_versions_note_created ON note_versions(note_id, created_at DESC);
