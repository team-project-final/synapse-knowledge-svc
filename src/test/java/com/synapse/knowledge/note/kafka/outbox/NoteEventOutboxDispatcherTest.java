package com.synapse.knowledge.note.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private NoteEventPublisher noteEventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("dispatchPending_발행성공이면_shouldOutbox상태를PUBLISHED로변경한다")
    void dispatchPending_발행성공이면_shouldOutbox상태를PUBLISHED로변경한다() throws Exception {
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
            "knowledge.note.note-created-v1",
            "tenant-1",
            NoteEventOutboxService.EVENT_TYPE_CREATED,
            objectMapper.writeValueAsString(payload)
        );
        NoteEventOutboxDispatcher noteEventOutboxDispatcher = new NoteEventOutboxDispatcher(
            noteEventOutboxRepository,
            noteEventPublisher,
            objectMapper
        );
        ReflectionTestUtils.setField(noteEventOutboxDispatcher, "batchSize", 50);
        given(noteEventOutboxRepository.findByStatusOrderByIdAsc(eq(NoteEventOutboxStatus.PENDING), any()))
            .willReturn(List.of(outbox));
        given(noteEventPublisher.publishCreated(any(NoteCreatedPublishRequested.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventOutboxDispatcher.dispatchPending();

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getStatus()).isEqualTo(NoteEventOutboxStatus.PUBLISHED);
        assertThat(outboxCaptor.getValue().getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("dispatchPending_발행실패이면_shouldPending상태로재시도정보를남긴다")
    void dispatchPending_발행실패이면_shouldPending상태로재시도정보를남긴다() throws Exception {
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
            "knowledge.note.note-created-v1",
            "tenant-2",
            NoteEventOutboxService.EVENT_TYPE_CREATED,
            objectMapper.writeValueAsString(payload)
        );
        NoteEventOutboxDispatcher noteEventOutboxDispatcher = new NoteEventOutboxDispatcher(
            noteEventOutboxRepository,
            noteEventPublisher,
            objectMapper
        );
        ReflectionTestUtils.setField(noteEventOutboxDispatcher, "batchSize", 50);
        given(noteEventOutboxRepository.findByStatusOrderByIdAsc(eq(NoteEventOutboxStatus.PENDING), any()))
            .willReturn(List.of(outbox));
        CompletableFuture<SendResult<String, SpecificRecord>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalStateException("broker down"));
        given(noteEventPublisher.publishCreated(any(NoteCreatedPublishRequested.class)))
            .willReturn(failedFuture);

        noteEventOutboxDispatcher.dispatchPending();

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().getStatus()).isEqualTo(NoteEventOutboxStatus.PENDING);
        assertThat(outboxCaptor.getValue().getAttemptCount()).isEqualTo(1);
        assertThat(outboxCaptor.getValue().getLastError()).contains("broker down");
    }
}
