package com.synapse.knowledge.note.kafka.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Mock
    private NoteEventOutboxRepository noteEventOutboxRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("enqueueCreated_정상요청이면_shouldOutboxRow를저장한다")
    void enqueueCreated_정상요청이면_shouldOutboxRow를저장한다() throws Exception {
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of("tag"));
        UUID externalNoteId = UUID.randomUUID();
        String eventUserId = "11111111-1111-1111-1111-111111111111";
        ReflectionTestUtils.setField(note, "createdAt", LocalDateTime.of(2026, 6, 1, 9, 0));

        NoteEventOutboxService service = new NoteEventOutboxService(noteEventOutboxRepository, objectMapper);

        service.enqueueCreated(note, externalNoteId, eventUserId);

        ArgumentCaptor<NoteEventOutbox> outboxCaptor = ArgumentCaptor.forClass(NoteEventOutbox.class);
        verify(noteEventOutboxRepository).save(outboxCaptor.capture());
        NoteEventOutbox outbox = outboxCaptor.getValue();
        assertThat(outbox.getTopic()).isEqualTo("knowledge.note.note-created-v1");
        assertThat(outbox.getMessageKey()).isEqualTo("tenant-1");
        assertThat(outbox.getEventType()).isEqualTo(NoteEventOutboxService.EVENT_TYPE_CREATED);
        NoteCreatedPublishRequested payload = objectMapper.readValue(outbox.getPayloadJson(), NoteCreatedPublishRequested.class);
        assertThat(payload.externalNoteId()).isEqualTo(externalNoteId);
        assertThat(payload.userId()).isEqualTo(eventUserId);
        assertThat(payload.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("enqueueCreated_같은eventId가입력되면_should예외없이중복을건너뛴다")
    void enqueueCreated_같은eventId가입력되면_should예외없이중복을건너뛴다() {
        Note note = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of());
        UUID externalNoteId = UUID.randomUUID();
        String eventUserId = "11111111-1111-1111-1111-111111111111";
        NoteEventOutboxService service = new NoteEventOutboxService(noteEventOutboxRepository, objectMapper);
        given(noteEventOutboxRepository.save(any(NoteEventOutbox.class)))
            .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatNoException().isThrownBy(() -> service.enqueueCreated(note, externalNoteId, eventUserId));
    }
}
