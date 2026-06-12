package com.synapse.knowledge.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "synapse.kafka")
public record KafkaTopicProperties(String topicPrefix) {

    public KafkaTopicProperties {
        topicPrefix = topicPrefix == null ? "" : topicPrefix;
    }
}
