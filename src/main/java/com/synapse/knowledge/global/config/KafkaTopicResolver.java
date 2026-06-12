package com.synapse.knowledge.global.config;

public class KafkaTopicResolver {

    static final String NOTE_CREATED_BASE = "knowledge.note.note-created-v1";
    static final String NOTE_UPDATED_BASE = "knowledge.note.note-updated-v1";
    static final String NOTE_SEARCH_SYNC_BASE = "knowledge.note.note-search-sync-v1";

    private final KafkaTopicProperties properties;

    public KafkaTopicResolver(KafkaTopicProperties properties) {
        this.properties = properties;
    }

    public String noteCreated() {
        return resolve(NOTE_CREATED_BASE);
    }

    public String noteUpdated() {
        return resolve(NOTE_UPDATED_BASE);
    }

    public String noteSearchSync() {
        return resolve(NOTE_SEARCH_SYNC_BASE);
    }

    public String dlq(String resolvedTopic) {
        return resolvedTopic + ".dlq";
    }

    private String resolve(String baseTopic) {
        return properties.topicPrefix() + baseTopic;
    }
}
