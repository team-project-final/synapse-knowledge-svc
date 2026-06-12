package com.synapse.knowledge.note.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.global.config.KafkaTopicProperties;
import com.synapse.knowledge.global.config.KafkaTopicResolver;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.kafka.producer.NoteCreatedPublishRequested;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteEventOutboxServiceTest {

    private static final KafkaTopicResolver KAFKA_TOPIC_RESOLVER =
        new KafkaTopicResolver(new KafkaTopicProperties("dev."));

    @Mock
    private NoteEventOutboxRepository noteEventOutboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("정상 요청이면 Outbox Row를 저장한다")
    void enqueueCreated_validRequest_shouldSaveOutboxRow() throws Exception {
        String deckId = "550e8400-e29b-41d4-a716-446655440000";
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of("tag"), deckId);
        UUID externalNoteId = UUID.randomUUID();
        String eventUserId = "11111111-1111-1111-1111-111111111111";
        ReflectionTestUtils.setField(note, "createdAt", LocalDateTime.of(2026, 6, 1, 9, 0));

        NoteEventOutboxService service = new NoteEventOutboxService(
            noteEventOutboxRepository,
            objectMapper,
            KAFKA_TOPIC_RESOLVER
        );
        ReflectionTestUtils.setField(service, "kafkaEnabled", true);

        service.enqueueCreated(note, externalNoteId, eventUserId);

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        NoteEventOutbox outbox = outboxCaptor.getValue();
        assertThat(outbox.getTopic()).isEqualTo("dev.knowledge.note.note-created-v1");
        assertThat(outbox.getMessageKey()).isEqualTo("tenant-1");
        assertThat(outbox.getEventType()).isEqualTo(NoteEventOutboxService.EVENT_TYPE_CREATED);
        NoteCreatedPublishRequested payload = objectMapper.readValue(outbox.getPayloadJson(), NoteCreatedPublishRequested.class);
        assertThat(payload.externalNoteId()).isEqualTo(externalNoteId);
        assertThat(payload.userId()).isEqualTo(eventUserId);
        assertThat(payload.title()).isEqualTo("제목");
        assertThat(payload.deckId()).isEqualTo(deckId);
    }

    @Test
    @DisplayName("같은 eventId가 입력되면 예외 없이 중복을 건너뛴다")
    void enqueueCreated_duplicateEventId_shouldSkipWithoutException() {
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of());
        UUID externalNoteId = UUID.randomUUID();
        String eventUserId = "11111111-1111-1111-1111-111111111111";
        NoteEventOutboxService service = new NoteEventOutboxService(
            noteEventOutboxRepository,
            objectMapper,
            KAFKA_TOPIC_RESOLVER
        );
        ReflectionTestUtils.setField(service, "kafkaEnabled", true);
        given(noteEventOutboxRepository.save(any(NoteEventOutbox.class)))
            .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatNoException().isThrownBy(() -> service.enqueueCreated(note, externalNoteId, eventUserId));
    }

    @Test
    @DisplayName("Kafka가 비활성화면 Outbox Row를 저장하지 않는다")
    void enqueueCreated_kafkaDisabled_shouldNotSaveOutboxRow() {
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of("tag"));
        UUID externalNoteId = UUID.randomUUID();
        String eventUserId = "11111111-1111-1111-1111-111111111111";

        NoteEventOutboxService service = new NoteEventOutboxService(
            noteEventOutboxRepository,
            objectMapper,
            KAFKA_TOPIC_RESOLVER
        );
        ReflectionTestUtils.setField(service, "kafkaEnabled", false);

        service.enqueueCreated(note, externalNoteId, eventUserId);

        verify(noteEventOutboxRepository, never()).save(any(NoteEventOutbox.class));
    }
}
