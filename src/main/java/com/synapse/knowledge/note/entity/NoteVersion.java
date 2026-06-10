package com.synapse.knowledge.note.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "note_versions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long noteId;

    @Column(nullable = false)
    private Integer versionNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentMd;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static NoteVersion of(Long noteId, int versionNo, String title, String contentMd) {
        NoteVersion v = new NoteVersion();
        v.noteId = noteId;
        v.versionNo = versionNo;
        v.title = title;
        v.contentMd = contentMd;
        v.createdAt = LocalDateTime.now();
        return v;
    }
}
