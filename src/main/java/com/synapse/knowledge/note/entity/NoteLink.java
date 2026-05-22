package com.synapse.knowledge.note.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "note_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_note_id", nullable = false)
    private Long sourceNoteId;

    @Column(name = "target_note_id")
    private Long targetNoteId;

    @Column(nullable = false, length = 200)
    private String targetTitle;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static NoteLink create(Long sourceNoteId, Long targetNoteId, String targetTitle) {
        NoteLink link = new NoteLink();
        link.sourceNoteId = sourceNoteId;
        link.targetNoteId = targetNoteId;
        link.targetTitle = targetTitle;
        link.createdAt = LocalDateTime.now();
        return link;
    }
}
