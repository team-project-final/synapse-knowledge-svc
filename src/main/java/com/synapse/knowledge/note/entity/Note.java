package com.synapse.knowledge.note.entity;

import com.synapse.knowledge.shared.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag", nullable = false, length = 30)
    private List<String> tags = new ArrayList<>();

    @Column(length = 36)
    private String deckId;
    
    @Column(nullable = false, length = 20)
    private String status;
    
    private Integer wordCount;
    
    private LocalDateTime deletedAt;

    public static Note create(
        String tenantId,
        Long userId,
        String title,
        String contentMd,
        String contentPlain,
        List<String> tags
    ) {
        return create(tenantId, userId, title, contentMd, contentPlain, tags, null);
    }

    public static Note create(
        String tenantId,
        Long userId,
        String title,
        String contentMd,
        String contentPlain,
        List<String> tags,
        String deckId
    ) {
        Note note = new Note();
        note.tenantId = tenantId;
        note.userId = userId;
        note.title = title;
        note.contentMd = contentMd;
        note.contentPlain = contentPlain;
        note.tags = sanitizeTags(tags);
        note.deckId = deckId;
        note.status = "active";
        note.wordCount = contentPlain != null ? contentPlain.length() : 0;
        return note;
    }

    public void update(String title, String contentMd, String contentPlain, List<String> tags) {
        update(title, contentMd, contentPlain, tags, this.deckId);
    }

    public void update(String title, String contentMd, String contentPlain, List<String> tags, String deckId) {
        this.title = title;
        this.contentMd = contentMd;
        this.contentPlain = contentPlain;
        this.tags.clear();
        this.tags.addAll(sanitizeTags(tags));
        this.deckId = deckId;
        this.wordCount = contentPlain != null ? contentPlain.length() : 0;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    private static List<String> sanitizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(tags.stream()
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(tag -> !tag.isBlank())
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
    }
}
