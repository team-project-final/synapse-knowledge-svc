package com.synapse.knowledge.search.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConfigTest {

    private final KafkaConfig kafkaConfig = new KafkaConfig(mock(SlackNotifier.class));
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(KafkaConfig.class, KafkaConfigTestSupport.class);

    KafkaConfigTest() {
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "PLAINTEXT");
    }

    @Test
    @DisplayName("searchSyncKafkaTemplate_SSL설정_shouldSecurityProtocol포함")
    void searchSyncKafkaTemplate_SSL설정_shouldSecurityProtocol포함() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "SSL");

        // When
        KafkaTemplate<Object, Object> kafkaTemplate = kafkaConfig.searchSyncKafkaTemplate(
            "b-1.example.amazonaws.com:9094"
        );
        DefaultKafkaProducerFactory<Object, Object> producerFactory =
            (DefaultKafkaProducerFactory<Object, Object>) kafkaTemplate.getProducerFactory();
        Map<String, Object> props = producerFactory.getConfigurationProperties();

        // Then
        assertThat(props).containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    }

    @Test
    @DisplayName("searchSyncConsumerFactory_SSL설정_shouldSecurityProtocol포함")
    void searchSyncConsumerFactory_SSL설정_shouldSecurityProtocol포함() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "SSL");

        // When
        ConsumerFactory<String, NoteSearchSyncKafkaEvent> consumerFactory =
            kafkaConfig.searchSyncConsumerFactory(
                "b-1.example.amazonaws.com:9094",
                "knowledge-search-indexer"
            );
        Map<String, Object> props =
            ((DefaultKafkaConsumerFactory<String, NoteSearchSyncKafkaEvent>) consumerFactory)
                .getConfigurationProperties();

        // Then
        assertThat(props).containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    }

    @Test
    @DisplayName("searchSyncConsumerFactory_PLAINTEXT기본값_shouldSecurityProtocol미포함")
    void searchSyncConsumerFactory_PLAINTEXT기본값_shouldSecurityProtocol미포함() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "PLAINTEXT");

        // When
        ConsumerFactory<String, NoteSearchSyncKafkaEvent> consumerFactory =
            kafkaConfig.searchSyncConsumerFactory(
                "localhost:9092",
                "knowledge-search-indexer"
            );
        Map<String, Object> props =
            ((DefaultKafkaConsumerFactory<String, NoteSearchSyncKafkaEvent>) consumerFactory)
                .getConfigurationProperties();

        // Then
        assertThat(props).doesNotContainKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
    }

    @Test
    @DisplayName("searchSyncConsumerFactory_autoOffsetReset설정_shouldConfiguredValue사용")
    void searchSyncConsumerFactory_autoOffsetReset설정_shouldConfiguredValue사용() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "latest");

        // When
        ConsumerFactory<String, NoteSearchSyncKafkaEvent> consumerFactory =
            kafkaConfig.searchSyncConsumerFactory(
                "localhost:9092",
                "knowledge-search-indexer-e2e"
            );
        Map<String, Object> props =
            ((DefaultKafkaConsumerFactory<String, NoteSearchSyncKafkaEvent>) consumerFactory)
                .getConfigurationProperties();

        // Then
        assertThat(props).containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    }

    @Test
    @DisplayName("synapseKafkaEnabled_false면SearchKafkaBean을등록하지않는다")
    void context_synapseKafkaEnabledFalse_shouldNotRegisterSearchKafkaBeans() {
        contextRunner
            .withPropertyValues("synapse.kafka.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean("searchSyncKafkaTemplate");
                assertThat(context).doesNotHaveBean("searchSyncConsumerFactory");
                assertThat(context).doesNotHaveBean("searchSyncKafkaListenerContainerFactory");
            });
    }

    @Test
    @DisplayName("synapseKafkaEnabled_true면SearchKafkaBean을등록한다")
    void context_synapseKafkaEnabledTrue_shouldRegisterSearchKafkaBeans() {
        contextRunner
            .withPropertyValues(
                "synapse.kafka.enabled=true",
                "spring.kafka.bootstrap-servers=localhost:9092"
            )
            .run(context -> {
                assertThat(context).hasBean("searchSyncKafkaTemplate");
                assertThat(context).hasBean("searchSyncConsumerFactory");
                assertThat(context).hasBean("searchSyncKafkaListenerContainerFactory");
            });
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class KafkaConfigTestSupport {

        @Bean
        SlackNotifier slackNotifier() {
            return mock(SlackNotifier.class);
        }
    }
}
