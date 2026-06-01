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

    @Mock
    private KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @InjectMocks
    private NoteEventPublisher noteEventPublisher;

    @Test
    @DisplayName("handle_노트생성이벤트를받으면_shouldNoteCreated토픽으로발행")
    void handle_노트생성이벤트를받으면_shouldNoteCreated토픽으로발행() {
        NoteCreatedPublishRequested event = new NoteCreatedPublishRequested(
            "event-1",
            UUID.randomUUID(),
            101L,
            "tenant-a",
            "새 노트",
            "평문 내용",
            "2026-06-01T10:15:30",
            1_717_234_530_000L,
            null
        );
        given(kafkaTemplate.send(eq(NoteKafkaTopics.NOTE_CREATED), eq("tenant-a"), org.mockito.ArgumentMatchers.any(SpecificRecord.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventPublisher.handle(event);

        ArgumentCaptor<SpecificRecord> payloadCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        verify(kafkaTemplate).send(eq(NoteKafkaTopics.NOTE_CREATED), eq("tenant-a"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isInstanceOf(NoteCreated.class);
        NoteCreated payload = (NoteCreated) payloadCaptor.getValue();
        assertThat(payload.getEventId()).isEqualTo("event-1");
        assertThat(payload.getNoteId()).isEqualTo(event.externalNoteId().toString());
        assertThat(payload.getUserId()).isEqualTo("101");
        assertThat(payload.getTenantId()).isEqualTo("tenant-a");
        assertThat(payload.getTitle()).isEqualTo("새 노트");
        assertThat(payload.getContent()).isEqualTo("평문 내용");
        assertThat(payload.getDeckId()).isNull();
    }

    @Test
    @DisplayName("handle_노트수정이벤트를받으면_shouldNoteUpdated토픽으로발행")
    void handle_노트수정이벤트를받으면_shouldNoteUpdated토픽으로발행() {
        NoteUpdatedPublishRequested event = new NoteUpdatedPublishRequested(
            "event-2",
            UUID.randomUUID(),
            202L,
            "tenant-b",
            "수정 노트",
            "2026-06-01T11:30:00",
            1_717_238_200_000L
        );
        given(kafkaTemplate.send(eq(NoteKafkaTopics.NOTE_UPDATED), eq("tenant-b"), org.mockito.ArgumentMatchers.any(SpecificRecord.class)))
            .willReturn(CompletableFuture.completedFuture(null));

        noteEventPublisher.handle(event);

        ArgumentCaptor<SpecificRecord> payloadCaptor = ArgumentCaptor.forClass(SpecificRecord.class);
        verify(kafkaTemplate).send(eq(NoteKafkaTopics.NOTE_UPDATED), eq("tenant-b"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isInstanceOf(NoteUpdated.class);
        NoteUpdated payload = (NoteUpdated) payloadCaptor.getValue();
        assertThat(payload.getEventId()).isEqualTo("event-2");
        assertThat(payload.getNoteId()).isEqualTo(event.externalNoteId().toString());
        assertThat(payload.getUserId()).isEqualTo("202");
        assertThat(payload.getTenantId()).isEqualTo("tenant-b");
        assertThat(payload.getTitle()).isEqualTo("수정 노트");
        assertThat(payload.getUpdatedAt()).isEqualTo("2026-06-01T11:30:00");
    }
}
