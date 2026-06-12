package com.synapse.knowledge.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    @Bean
    KafkaTopicResolver kafkaTopicResolver(KafkaTopicProperties kafkaTopicProperties) {
        return new KafkaTopicResolver(kafkaTopicProperties);
    }
}
