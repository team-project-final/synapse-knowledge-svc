ALTER TABLE note_event_outbox
    ADD COLUMN claimed_by VARCHAR(100),
    ADD COLUMN claim_expires_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_note_event_outbox_status_claim_id
    ON note_event_outbox (status, claim_expires_at, id);
