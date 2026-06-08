package com.synapse.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.note.kafka.producer.NoteEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class KnowledgeSvcApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext.getBeansOfType(NoteEventPublisher.class)).isEmpty();
        assertThat(applicationContext.containsBean("searchSyncKafkaConfig")).isFalse();
        assertThat(applicationContext.containsBean("searchSyncKafkaTemplate")).isFalse();
        assertThat(applicationContext.containsBean("noteSearchKafkaProducer")).isFalse();
        assertThat(applicationContext.containsBean("noteSearchKafkaConsumer")).isFalse();
        assertThat(applicationContext.containsBean("noteEventOutboxDispatcher")).isFalse();
    }

}
