package com.synapse.knowledge.note.kafka.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import com.synapse.knowledge.NoteCreated;
import com.synapse.knowledge.NoteUpdated;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.avro.specific.SpecificRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
@ExtendWith(MockitoExtension.class)
class NoteEventPublisherTest {

    private static final String CREATED_TOPIC = "dev.knowledge.note.note-created-v1";
    private static final String UPDATED_TOPIC = "dev.knowledge.note.note-updated-v1";

    @Mock
    private KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @InjectMocks
    private NoteEventPublisher noteEventPublisher;

    @Test
    @DisplayName("노트 생성 이벤트를 받으면 NoteCreated 토픽으로 발행한다")
    void handle_noteCreatedEvent_shouldPublishToNoteCreatedTopic() {
        String deckId = "550e8400-e29b-41d4-a716-446655440000";
        NoteCreatedPublishRequested event = new NoteCreatedPublishRequested(
            "event-1",
            UUID.randomUUID(),
            "11111111-1111-1111-1111-111111111111",
            "tenant-a",
            "새 노트",
            "평문 내용",
            "2026-06-01T10:15:30",
            1_717_234_530_000L,
            deckId
        );
        given(kafkaTemplate.send(eq(CREATED_TOPIC), eq("tenant-a"), org.mockito.ArgumentMatchers.any(SpecificRecord.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventPublisher.publishCreated(CREATED_TOPIC, event);

        ArgumentCaptor<SpecificRecord> payloadCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        verify(kafkaTemplate).send(eq(CREATED_TOPIC), eq("tenant-a"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isInstanceOf(NoteCreated.class);
        NoteCreated payload = (NoteCreated) payloadCaptor.getValue();
        assertThat(payload.getEventId()).isEqualTo("event-1");
        assertThat(payload.getNoteId()).isEqualTo(event.externalNoteId().toString());
        assertThat(payload.getUserId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(payload.getTenantId()).isEqualTo("tenant-a");
        assertThat(payload.getTitle()).isEqualTo("새 노트");
        assertThat(payload.getContent()).isEqualTo("평문 내용");
        assertThat(payload.getDeckId()).isEqualTo(deckId);
    }

    @Test
    @DisplayName("노트 수정 이벤트를 받으면 NoteUpdated 토픽으로 발행한다")
    void handle_noteUpdatedEvent_shouldPublishToNoteUpdatedTopic() {
        NoteUpdatedPublishRequested event = new NoteUpdatedPublishRequested(
            "event-2",
            UUID.randomUUID(),
            "22222222-2222-2222-2222-222222222222",
            "tenant-b",
            "수정 노트",
            "2026-06-01T11:30:00",
            1_717_238_200_000L
        );
        given(kafkaTemplate.send(eq(UPDATED_TOPIC), eq("tenant-b"), org.mockito.ArgumentMatchers.any(SpecificRecord.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventPublisher.publishUpdated(UPDATED_TOPIC, event);

        ArgumentCaptor<SpecificRecord> payloadCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        verify(kafkaTemplate).send(eq(UPDATED_TOPIC), eq("tenant-b"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isInstanceOf(NoteUpdated.class);
        NoteUpdated payload = (NoteUpdated) payloadCaptor.getValue();
        assertThat(payload.getEventId()).isEqualTo("event-2");
        assertThat(payload.getNoteId()).isEqualTo(event.externalNoteId().toString());
        assertThat(payload.getUserId()).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(payload.getTenantId()).isEqualTo("tenant-b");
        assertThat(payload.getTitle()).isEqualTo("수정 노트");
        assertThat(payload.getUpdatedAt()).isEqualTo("2026-06-01T11:30:00");
    }
}
