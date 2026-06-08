package com.synapse.knowledge.search.config;

import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration("searchSyncKafkaConfig")
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@SuppressWarnings({"deprecation", "removal"})
class KafkaConfig {

    private final SlackNotifier slackNotifier;

    @Value("${spring.kafka.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean listenerAutoStartup;

    @Bean("searchSyncKafkaTemplate")
    KafkaTemplate<Object, Object> searchSyncKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        applySecurityProtocol(props);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    ConsumerFactory<String, NoteSearchSyncKafkaEvent> searchSyncConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id:knowledge-search-indexer}") String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NoteSearchSyncKafkaEvent.class.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        applySecurityProtocol(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, NoteSearchSyncKafkaEvent>
        searchSyncKafkaListenerContainerFactory(
            ConsumerFactory<String, NoteSearchSyncKafkaEvent> searchSyncConsumerFactory,
            @Qualifier("searchSyncKafkaTemplate") KafkaTemplate<Object, Object> kafkaTemplate) {

        var backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxElapsedTime(7_000L);

        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> {
                slackNotifier.sendDlqAlert(record.topic(), ex.getMessage());
                return new TopicPartition(record.topic() + ".dlq", -1);
            });

        var factory = new ConcurrentKafkaListenerContainerFactory<String, NoteSearchSyncKafkaEvent>();
        factory.setConsumerFactory(searchSyncConsumerFactory);
        factory.setAutoStartup(listenerAutoStartup);
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));
        return factory;
    }

    private void applySecurityProtocol(Map<String, Object> props) {
        if (!"PLAINTEXT".equalsIgnoreCase(securityProtocol)) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        }
    }
}

