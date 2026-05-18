package com.synapse.knowledge.note.domain;

import com.synapse.knowledge.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String tenantId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentMd;
    
    @Column(columnDefinition = "TEXT")
    private String contentPlain;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    private Integer wordCount;
    
    private LocalDateTime deletedAt;

    public static Note create(String tenantId, Long userId, String title, String contentMd, String contentPlain) {
        Note note = new Note();
        note.tenantId = tenantId;
        note.userId = userId;
        note.title = title;
        note.contentMd = contentMd;
        note.contentPlain = contentPlain;
        note.status = "active";
        note.wordCount = contentPlain != null ? contentPlain.length() : 0;
        return note;
    }

    public void update(String title, String contentMd, String contentPlain) {
        this.title = title;
        this.contentMd = contentMd;
        this.contentPlain = contentPlain;
        this.wordCount = contentPlain != null ? contentPlain.length() : 0;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
