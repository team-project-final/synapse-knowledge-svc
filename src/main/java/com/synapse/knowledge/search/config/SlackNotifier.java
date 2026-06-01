package com.synapse.knowledge.search.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
class SlackNotifier {

    private final RestClient restClient;
    private final String webhookUrl;
    private final String dlqChannel;

    SlackNotifier(
            RestClient.Builder builder,
            @Value("${slack.webhook-url:}") String webhookUrl,
            @Value("${slack.dlq-channel:#synapse-dlq-alert}") String dlqChannel) {
        this.restClient = builder.build();
        this.webhookUrl = webhookUrl;
        this.dlqChannel = dlqChannel;
    }

    void sendDlqAlert(String topic, String errorMessage) {
        if (webhookUrl.isBlank()) {
            log.warn("DLQ alert skipped — SLACK_WEBHOOK_URL not configured. topic={}", topic);
            return;
        }
        try {
            String payload = String.format(
                "{\"channel\":\"%s\",\"text\":\"[DLQ Alert] topic=%s\\nerror=%s\"}",
                dlqChannel, topic, errorMessage
            );
            restClient.post()
                .uri(webhookUrl)
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send DLQ Slack alert: {}", e.getMessage());
        }
    }
}

