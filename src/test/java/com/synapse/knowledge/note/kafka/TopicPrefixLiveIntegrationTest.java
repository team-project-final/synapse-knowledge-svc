package com.synapse.knowledge.note.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.NoteCreated;
import com.synapse.knowledge.NoteUpdated;
import com.synapse.knowledge.global.config.KafkaTopicResolver;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.kafka.outbox.NoteEventOutboxClaimService;
import com.synapse.knowledge.note.kafka.outbox.NoteEventOutboxDispatcher;
import com.synapse.knowledge.note.kafka.outbox.NoteEventOutboxRepository;
import com.synapse.knowledge.note.kafka.producer.NoteEventPublisher;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(properties = {
    "synapse.kafka.enabled=true",
    "synapse.kafka.topic-prefix=dev.",
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.properties.schema.registry.url=http://localhost:8086",
    "spring.kafka.listener.auto-startup=false",
    "synapse.kafka.search-sync-listener.auto-startup=false"
})
@ActiveProfiles("test")
class TopicPrefixLiveIntegrationTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8086";
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteEventOutboxRepository noteEventOutboxRepository;

    @Autowired
    private NoteEventOutboxClaimService noteEventOutboxClaimService;

    @Autowired
    private KafkaTemplate<String, org.apache.avro.specific.SpecificRecord> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTopicResolver kafkaTopicResolver;

    @AfterEach
    void tearDown() {
        noteEventOutboxRepository.deleteAll();
    }

    @Test
    @DisplayName("노트 생성 시 dev prefix가 붙은 note-created와 search-sync topic으로 발행된다")
    void create_withTopicPrefix_shouldPublishCreatedAndSearchSyncToPrefixedTopics() {
        try (
            KafkaConsumer<String, Object> noteCreatedConsumer = avroConsumer();
            KafkaConsumer<String, NoteSearchSyncKafkaEvent> searchSyncConsumer = searchSyncConsumer()
        ) {
            String createdTopic = kafkaTopicResolver.noteCreated();
            String searchSyncTopic = kafkaTopicResolver.noteSearchSync();
            ensureTopicExists(createdTopic);
            ensureTopicExists(searchSyncTopic);
            subscribe(noteCreatedConsumer, createdTopic);
            subscribe(searchSyncConsumer, searchSyncTopic);

            NoteResponse created = noteService.create(
                101L,
                new NoteCreateRequest(
                    "tenant-prefix-live-create",
                    "prefix create title",
                    "prefix create body",
                    List.of("prefix", "create"),
                    null
                )
            );
            dispatchPendingOutbox();

            ConsumerRecord<String, Object> createdRecord = pollUntilRecord(noteCreatedConsumer, createdTopic);
            ConsumerRecord<String, NoteSearchSyncKafkaEvent> searchSyncRecord =
                pollUntilRecord(searchSyncConsumer, searchSyncTopic);

            assertThat(createdRecord.topic()).isEqualTo("dev.knowledge.note.note-created-v1");
            assertThat(createdRecord.value()).isInstanceOf(NoteCreated.class);
            NoteCreated noteCreated = (NoteCreated) createdRecord.value();
            assertThat(noteCreated.getTitle()).isEqualTo("prefix create title");
            assertThat(noteCreated.getTenantId()).isEqualTo("tenant-prefix-live-create");
            assertThat(noteCreated.getNoteId()).isNotBlank();

            assertThat(searchSyncRecord.topic()).isEqualTo("dev.knowledge.note.note-search-sync-v1");
            assertThat(searchSyncRecord.value().noteId()).isEqualTo(created.id());
            assertThat(searchSyncRecord.value().title()).isEqualTo("prefix create title");
            assertThat(searchSyncRecord.value().deleted()).isFalse();
        }
    }

    @Test
    @DisplayName("노트 수정 시 dev prefix가 붙은 note-updated와 search-sync topic으로 발행된다")
    void update_withTopicPrefix_shouldPublishUpdatedAndSearchSyncToPrefixedTopics() {
        NoteResponse created = noteService.create(
            202L,
            new NoteCreateRequest(
                "tenant-prefix-live-update",
                "before update title",
                "before update body",
                List.of("prefix", "before"),
                null
            )
        );
        dispatchPendingOutbox();

        try (
            KafkaConsumer<String, Object> noteUpdatedConsumer = avroConsumer();
            KafkaConsumer<String, NoteSearchSyncKafkaEvent> searchSyncConsumer = searchSyncConsumer()
        ) {
            String updatedTopic = kafkaTopicResolver.noteUpdated();
            String searchSyncTopic = kafkaTopicResolver.noteSearchSync();
            ensureTopicExists(updatedTopic);
            ensureTopicExists(searchSyncTopic);
            subscribe(noteUpdatedConsumer, updatedTopic);
            subscribe(searchSyncConsumer, searchSyncTopic);

            noteService.update(
                202L,
                created.id(),
                new NoteCreateRequest(
                    "tenant-prefix-live-update",
                    "after update title",
                    "after update body",
                    List.of("prefix", "after"),
                    null
                )
            );
            dispatchPendingOutbox();

            ConsumerRecord<String, Object> updatedRecord = pollUntilRecord(noteUpdatedConsumer, updatedTopic);
            ConsumerRecord<String, NoteSearchSyncKafkaEvent> searchSyncRecord =
                pollUntilRecord(searchSyncConsumer, searchSyncTopic);

            assertThat(updatedRecord.topic()).isEqualTo("dev.knowledge.note.note-updated-v1");
            assertThat(updatedRecord.value()).isInstanceOf(NoteUpdated.class);
            NoteUpdated noteUpdated = (NoteUpdated) updatedRecord.value();
            assertThat(noteUpdated.getTitle()).isEqualTo("after update title");
            assertThat(noteUpdated.getTenantId()).isEqualTo("tenant-prefix-live-update");
            assertThat(noteUpdated.getNoteId()).isNotBlank();

            assertThat(searchSyncRecord.topic()).isEqualTo("dev.knowledge.note.note-search-sync-v1");
            assertThat(searchSyncRecord.value().noteId()).isEqualTo(created.id());
            assertThat(searchSyncRecord.value().title()).isEqualTo("after update title");
            assertThat(searchSyncRecord.value().deleted()).isFalse();
        }
    }

    private void dispatchPendingOutbox() {
        NoteEventOutboxDispatcher dispatcher = new NoteEventOutboxDispatcher(
            noteEventOutboxRepository,
            noteEventOutboxClaimService,
            new NoteEventPublisher(kafkaTemplate),
            objectMapper
        );
        ReflectionTestUtils.setField(dispatcher, "batchSize", 50);
        dispatcher.dispatchPending();
    }

    private KafkaConsumer<String, Object> avroConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "topic-prefix-avro-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new KafkaConsumer<>(props);
    }

    private KafkaConsumer<String, NoteSearchSyncKafkaEvent> searchSyncConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "topic-prefix-search-sync-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NoteSearchSyncKafkaEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new KafkaConsumer<>(props);
    }

    private <T> void subscribe(KafkaConsumer<String, T> consumer, String topic) {
        consumer.subscribe(List.of(topic));
        Instant deadline = Instant.now().plus(POLL_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            consumer.poll(POLL_INTERVAL);
            if (!consumer.assignment().isEmpty()) {
                return;
            }
        }
        throw new IllegalStateException("No partition assignment for topic within timeout: " + topic);
    }

    private <T> ConsumerRecord<String, T> pollUntilRecord(KafkaConsumer<String, T> consumer, String expectedTopic) {
        Instant deadline = Instant.now().plus(POLL_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            for (ConsumerRecord<String, T> record : consumer.poll(POLL_INTERVAL)) {
                if (expectedTopic.equals(record.topic())) {
                    return record;
                }
            }
        }
        throw new IllegalStateException("No record consumed from topic within timeout: " + expectedTopic);
    }

    private void ensureTopicExists(String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        try (AdminClient adminClient = AdminClient.create(props)) {
            if (adminClient.listTopics().names().get(10, TimeUnit.SECONDS).contains(topic)) {
                return;
            }
            adminClient.createTopics(List.of(new NewTopic(topic, 1, (short) 1))).all().get(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to ensure Kafka topic exists: " + topic, ex);
        }
    }
}
