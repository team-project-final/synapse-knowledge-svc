package com.synapse.knowledge.note.kafka.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "note_event_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoteEventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "message_key", nullable = false, length = 100)
    private String messageKey;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NoteEventOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    @Column(name = "claim_expires_at")
    private LocalDateTime claimExpiresAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static NoteEventOutbox pending(
        String eventId,
        String topic,
        String messageKey,
        String eventType,
        String payloadJson
    ) {
        NoteEventOutbox outbox = new NoteEventOutbox();
        outbox.eventId = eventId;
        outbox.topic = topic;
        outbox.messageKey = messageKey;
        outbox.eventType = eventType;
        outbox.payloadJson = payloadJson;
        outbox.status = NoteEventOutboxStatus.PENDING;
        outbox.attemptCount = 0;
        return outbox;
    }

    public void markPublished() {
        this.status = NoteEventOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.lastError = null;
        this.attemptCount += 1;
        clearClaim();
    }

    public void markInProgress(String workerId, LocalDateTime leaseUntil) {
        this.status = NoteEventOutboxStatus.IN_PROGRESS;
        this.claimedBy = workerId;
        this.claimExpiresAt = leaseUntil;
    }

    public void recordFailure(String message) {
        this.status = NoteEventOutboxStatus.PENDING;
        this.lastError = truncate(message);
        this.attemptCount += 1;
        clearClaim();
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private String truncate(String message) {
        if (message == null || message.length() <= 1000) {
            return message;
        }
        return message.substring(0, 1000);
    }

    private void clearClaim() {
        this.claimedBy = null;
        this.claimExpiresAt = null;
    }
}
