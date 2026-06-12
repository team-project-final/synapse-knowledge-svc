package com.synapse.knowledge.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaTopicResolverTest {

    @Test
    @DisplayName("prefix가 비어있으면 base topic을 그대로 반환한다")
    void noteTopics_withoutPrefix_shouldReturnBaseTopics() {
        KafkaTopicResolver resolver = new KafkaTopicResolver(new KafkaTopicProperties(""));

        assertThat(resolver.noteCreated()).isEqualTo("knowledge.note.note-created-v1");
        assertThat(resolver.noteUpdated()).isEqualTo("knowledge.note.note-updated-v1");
        assertThat(resolver.noteSearchSync()).isEqualTo("knowledge.note.note-search-sync-v1");
    }

    @Test
    @DisplayName("prefix가 있으면 모든 topic 앞에 붙여 반환한다")
    void noteTopics_withPrefix_shouldReturnPrefixedTopics() {
        KafkaTopicResolver resolver = new KafkaTopicResolver(new KafkaTopicProperties("dev."));

        assertThat(resolver.noteCreated()).isEqualTo("dev.knowledge.note.note-created-v1");
        assertThat(resolver.noteUpdated()).isEqualTo("dev.knowledge.note.note-updated-v1");
        assertThat(resolver.noteSearchSync()).isEqualTo("dev.knowledge.note.note-search-sync-v1");
    }

    @Test
    @DisplayName("DLQ topic은 해석된 원본 topic 뒤에 dlq suffix를 붙인다")
    void dlq_withResolvedTopic_shouldAppendDlqSuffix() {
        KafkaTopicResolver resolver = new KafkaTopicResolver(new KafkaTopicProperties("staging."));

        assertThat(resolver.dlq("staging.knowledge.note.note-search-sync-v1"))
            .isEqualTo("staging.knowledge.note.note-search-sync-v1.dlq");
    }
}
