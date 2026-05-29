package com.synapse.knowledge.note.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "note_identity_map")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteIdentityMap {

    @Id
    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "external_note_id", nullable = false, unique = true)
    private UUID externalNoteId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static NoteIdentityMap create(Long noteId) {
        NoteIdentityMap mapping = new NoteIdentityMap();
        mapping.noteId = noteId;
        mapping.externalNoteId = UUID.randomUUID();
        mapping.createdAt = LocalDateTime.now();
        return mapping;
    }
}
