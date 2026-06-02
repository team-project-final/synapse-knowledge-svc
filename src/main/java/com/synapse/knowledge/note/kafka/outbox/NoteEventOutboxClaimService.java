package com.synapse.knowledge.note.kafka.outbox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteEventOutboxClaimService {

    private final NoteEventOutboxRepository noteEventOutboxRepository;

    @Value("${synapse.kafka.outbox.lease-ms:30000}")
    private long leaseMs;

    @Value("${spring.application.name:synapse-knowledge-svc}")
    private String applicationName;

    private final String workerId = UUID.randomUUID().toString();

    @Transactional(propagation = Propagation.REQUIRED)
    public List<NoteEventOutbox> claimNextBatch(int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime leaseUntil = now.plus(Duration.ofMillis(leaseMs));

        List<NoteEventOutbox> outboxes = noteEventOutboxRepository.findClaimCandidates(now, batchSize);
        String claimedBy = applicationName + ":" + workerId;
        outboxes.forEach(outbox -> outbox.markInProgress(claimedBy, leaseUntil));
        return noteEventOutboxRepository.saveAll(outboxes);
    }
}
