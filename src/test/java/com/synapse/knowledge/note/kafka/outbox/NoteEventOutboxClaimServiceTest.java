package com.synapse.knowledge.note.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteEventOutboxClaimServiceTest {

    @Mock
    private NoteEventOutboxRepository noteEventOutboxRepository;

    @Test
    @DisplayName("claimNextBatch_대기중아웃박스가있으면_shouldInProgress와Lease를설정한다")
    void claimNextBatch_대기중아웃박스가있으면_shouldInProgress와Lease를설정한다() {
        NoteEventOutbox outbox = NoteEventOutbox.pending(
            "event-1",
            "knowledge.note.note-created-v1",
            "tenant-1",
            NoteEventOutboxService.EVENT_TYPE_CREATED,
            "{\"eventId\":\"event-1\"}"
        );
        NoteEventOutboxClaimService claimService = new NoteEventOutboxClaimService(noteEventOutboxRepository);
        ReflectionTestUtils.setField(claimService, "leaseMs", 30_000L);
        ReflectionTestUtils.setField(claimService, "applicationName", "knowledge-test");
        given(noteEventOutboxRepository.findClaimCandidates(any(LocalDateTime.class), eq(10)))
            .willReturn(List.of(outbox));
        given(noteEventOutboxRepository.saveAll(List.of(outbox))).willReturn(List.of(outbox));

        List<NoteEventOutbox> claimed = claimService.claimNextBatch(10);

        verify(noteEventOutboxRepository).saveAll(List.of(outbox));
        assertThat(claimed).hasSize(1);
        assertThat(outbox.getStatus()).isEqualTo(NoteEventOutboxStatus.IN_PROGRESS);
        assertThat(outbox.getClaimedBy()).startsWith("knowledge-test:");
        assertThat(outbox.getClaimExpiresAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }
}
