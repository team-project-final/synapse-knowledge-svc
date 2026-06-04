package com.synapse.knowledge.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConfigTest {

    private final KafkaConfig kafkaConfig = new KafkaConfig();

    @Test
    @DisplayName("kafkaProducerFactory_SSL설정_shouldSecurityProtocol포함")
    void kafkaProducerFactory_SSL설정_shouldSecurityProtocol포함() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "SSL");

        // When
        DefaultKafkaProducerFactory<String, SpecificRecord> producerFactory =
            (DefaultKafkaProducerFactory<String, SpecificRecord>) kafkaConfig.kafkaProducerFactory(
                "b-1.example.amazonaws.com:9094",
                "http://localhost:8086",
                "all",
                3,
                1000,
                true
            );
        Map<String, Object> props = producerFactory.getConfigurationProperties();

        // Then
        assertThat(props).containsEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
    }

    @Test
    @DisplayName("kafkaProducerFactory_PLAINTEXT기본값_shouldSecurityProtocol미포함")
    void kafkaProducerFactory_PLAINTEXT기본값_shouldSecurityProtocol미포함() {
        // Given
        ReflectionTestUtils.setField(kafkaConfig, "securityProtocol", "PLAINTEXT");

        // When
        DefaultKafkaProducerFactory<String, SpecificRecord> producerFactory =
            (DefaultKafkaProducerFactory<String, SpecificRecord>) kafkaConfig.kafkaProducerFactory(
                "localhost:9092",
                "http://localhost:8086",
                "all",
                3,
                1000,
                true
            );
        Map<String, Object> props = producerFactory.getConfigurationProperties();

        // Then
        assertThat(props).doesNotContainKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
    }
}
