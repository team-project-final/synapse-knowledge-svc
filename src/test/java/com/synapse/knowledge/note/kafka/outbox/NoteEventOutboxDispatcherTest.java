package com.synapse.knowledge.note.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.kafka.producer.NoteCreatedPublishRequested;
import com.synapse.knowledge.note.kafka.producer.NoteEventPublisher;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteEventOutboxDispatcherTest {

    @Mock
    private NoteEventOutboxRepository noteEventOutboxRepository;

    @Mock
    private NoteEventOutboxClaimService noteEventOutboxClaimService;

    @Mock
    private NoteEventPublisher noteEventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("발행 성공이면 Outbox 상태를 PUBLISHED로 변경한다")
    void dispatchPending_publishSucceeds_shouldMarkOutboxAsPublished() throws Exception {
        NoteCreatedPublishRequested payload = new NoteCreatedPublishRequested(
            "event-1",
            UUID.randomUUID(),
            "11111111-1111-1111-1111-111111111111",
            "tenant-1",
            "제목",
            "본문",
            "2026-06-01T09:00:00",
            1_717_225_600_000L,
            null
        );
        NoteEventOutbox outbox = NoteEventOutbox.pending(
            payload.eventId(),
            "dev.knowledge.note.note-created-v1",
            "tenant-1",
            NoteEventOutboxService.EVENT_TYPE_CREATED,
            objectMapper.writeValueAsString(payload)
        );
        outbox.markInProgress("worker-1", java.time.LocalDateTime.now().plusSeconds(30));
        NoteEventOutboxDispatcher noteEventOutboxDispatcher = new NoteEventOutboxDispatcher(
            noteEventOutboxRepository,
            noteEventOutboxClaimService,
            noteEventPublisher,
            objectMapper
        );
        ReflectionTestUtils.setField(noteEventOutboxDispatcher, "batchSize", 50);
        given(noteEventOutboxClaimService.claimNextBatch(50))
            .willReturn(List.of(outbox));
        given(noteEventPublisher.publishCreated(anyString(), any(NoteCreatedPublishRequested.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventOutboxDispatcher.dispatchPending();

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        verify(noteEventPublisher).publishCreated("dev.knowledge.note.note-created-v1", payload);
        assertThat(outboxCaptor.getValue().getStatus()).isEqualTo(NoteEventOutboxStatus.PUBLISHED);
        assertThat(outboxCaptor.getValue().getPublishedAt()).isNotNull();
        assertThat(outboxCaptor.getValue().getClaimedBy()).isNull();
        assertThat(outboxCaptor.getValue().getClaimExpiresAt()).isNull();
    }

    @Test
    @DisplayName("발행 실패이면 Pending 상태로 재시도 정보를 남긴다")
    void dispatchPending_publishFails_shouldResetToPendingWithRetryInfo() throws Exception {
        NoteCreatedPublishRequested payload = new NoteCreatedPublishRequested(
            "event-2",
            UUID.randomUUID(),
            "22222222-2222-2222-2222-222222222222",
            "tenant-2",
            "제목",
            "본문",
            "2026-06-01T10:00:00",
            1_717_229_200_000L,
            null
        );
        NoteEventOutbox outbox = NoteEventOutbox.pending(
            payload.eventId(),
            "dev.knowledge.note.note-created-v1",
            "tenant-2",
            NoteEventOutboxService.EVENT_TYPE_CREATED,
            objectMapper.writeValueAsString(payload)
        );
        outbox.markInProgress("worker-2", java.time.LocalDateTime.now().plusSeconds(30));
        NoteEventOutboxDispatcher noteEventOutboxDispatcher = new NoteEventOutboxDispatcher(
            noteEventOutboxRepository,
            noteEventOutboxClaimService,
            noteEventPublisher,
            objectMapper
        );
        ReflectionTestUtils.setField(noteEventOutboxDispatcher, "batchSize", 50);
        given(noteEventOutboxClaimService.claimNextBatch(50))
            .willReturn(List.of(outbox));
        CompletableFuture<SendResult<String, SpecificRecord>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalStateException("broker down"));
        given(noteEventPublisher.publishCreated(anyString(), any(NoteCreatedPublishRequested.class)))
            .willReturn(failedFuture);

        noteEventOutboxDispatcher.dispatchPending();

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getStatus()).isEqualTo(NoteEventOutboxStatus.PENDING);
        assertThat(outboxCaptor.getValue().getAttemptCount()).isEqualTo(1);
        assertThat(outboxCaptor.getValue().getLastError()).contains("broker down");
        assertThat(outboxCaptor.getValue().getClaimedBy()).isNull();
        assertThat(outboxCaptor.getValue().getClaimExpiresAt()).isNull();
    }
}
