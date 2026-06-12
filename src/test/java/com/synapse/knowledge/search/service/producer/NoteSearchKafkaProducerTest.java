package com.synapse.knowledge.search.service.producer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.synapse.knowledge.global.config.KafkaTopicProperties;
import com.synapse.knowledge.global.config.KafkaTopicResolver;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class NoteSearchKafkaProducerTest {

    private static final String SEARCH_SYNC_TOPIC = "dev.knowledge.note.note-search-sync-v1";

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    private NoteSearchKafkaProducer noteSearchKafkaProducer;

    @BeforeEach
    void setUp() {
        noteSearchKafkaProducer = new NoteSearchKafkaProducer(
            kafkaTemplate,
            new KafkaTopicResolver(new KafkaTopicProperties("dev."))
        );
    }

    @Test
    @DisplayName("search sync 요청을 받으면 prefix가 붙은 topic으로 발행한다")
    void onNoteSearchSyncRequested_shouldPublishToPrefixedTopic() {
        NoteSearchSyncRequested event = new NoteSearchSyncRequested(
            1L,
            UUID.randomUUID(),
            "tenant-1",
            10L,
            "노트 제목",
            "본문",
            List.of("tag"),
            false,
            Instant.parse("2026-06-12T00:00:00Z")
        );

        noteSearchKafkaProducer.onNoteSearchSyncRequested(event);

        verify(kafkaTemplate).send(eq(SEARCH_SYNC_TOPIC), eq("1"), org.mockito.ArgumentMatchers.any());
    }
}
