package com.synapse.knowledge.search.service.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteSearchKafkaConsumerTest {

    @Mock
    private NoteSearchRepository noteSearchRepository;

    @Mock
    private KafkaIdempotencyStore idempotencyStore;

    @InjectMocks
    private NoteSearchKafkaConsumer consumer;

    @Test
    @DisplayName("upsert 이벤트 수신 시 ES에 인덱싱되고 멱등 키를 기록한다")
    void handle_upsertEvent_shouldIndexInESAndMarkProcessed() {
        given(idempotencyStore.isProcessed(anyString())).willReturn(false);
        var event = buildEvent(false);

        consumer.handle(event);

        verify(noteSearchRepository).upsert(any(NoteSearchDocument.class));
        verify(idempotencyStore).markProcessed(event.id());
    }

    @Test
    @DisplayName("delete 이벤트 수신 시 ES에서 삭제되고 멱등 키를 기록한다")
    void handle_deleteEvent_shouldDeleteFromESAndMarkProcessed() {
        given(idempotencyStore.isProcessed(anyString())).willReturn(false);
        var event = buildEvent(true);

        consumer.handle(event);

        verify(noteSearchRepository).deleteByNoteId(event.noteId());
        verify(idempotencyStore).markProcessed(event.id());
    }

    @Test
    @DisplayName("중복 이벤트 수신 시 ES 호출 없이 skip한다")
    void handle_duplicateEvent_shouldSkipWithoutESCall() {
        given(idempotencyStore.isProcessed(anyString())).willReturn(true);
        var event = buildEvent(false);

        consumer.handle(event);

        verify(idempotencyStore).isProcessed(event.id());
        verify(noteSearchRepository, never()).upsert(any());
        verify(idempotencyStore, never()).markProcessed(anyString());
    }

    @Test
    @DisplayName("tags가 null인 이벤트 수신 시 빈 리스트로 upsert한다")
    void handle_nullTagsEvent_shouldUpsertWithEmptyTagList() {
        given(idempotencyStore.isProcessed(anyString())).willReturn(false);
        var event = buildEventWithNullTags();

        consumer.handle(event);

        verify(noteSearchRepository).upsert(any(NoteSearchDocument.class));
        verify(idempotencyStore).markProcessed(event.id());
    }

    private NoteSearchSyncKafkaEvent buildEvent(boolean deleted) {
        return new NoteSearchSyncKafkaEvent(
            "1.0", UUID.randomUUID().toString(), "knowledge-svc",
            "com.synapse.event.knowledge.NoteSearchSyncRequested",
            Instant.now().toEpochMilli(), "tenant-1", "application/json",
            1L, UUID.randomUUID(), 10L, "테스트 노트", "테스트 내용",
            List.of("tag1"), deleted
        );
    }

    private NoteSearchSyncKafkaEvent buildEventWithNullTags() {
        return new NoteSearchSyncKafkaEvent(
            "1.0", UUID.randomUUID().toString(), "knowledge-svc",
            "com.synapse.event.knowledge.NoteSearchSyncRequested",
            Instant.now().toEpochMilli(), "tenant-1", "application/json",
            2L, UUID.randomUUID(), 10L, "태그없는 노트", "내용",
            null, false
        );
    }
}
