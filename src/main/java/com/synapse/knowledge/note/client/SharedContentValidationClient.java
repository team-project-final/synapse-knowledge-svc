package com.synapse.knowledge.note.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.global.exception.ExternalServiceException;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Engagement 내부 공유 검증 API 호출 클라이언트.
 * 사용자 JWT를 그대로 전달하며, 호출 실패(네트워크/5xx)는 {@link ExternalServiceException}(502)로 변환한다.
 */
@Component
public class SharedContentValidationClient {

    private static final String VALIDATE_PATH = "/api/v1/internal/shared-contents/validate";
    private static final String CONTENT_TYPE_NOTE = "NOTE";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public SharedContentValidationClient(
        @Qualifier("engagementRestClient") RestClient restClient,
        ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public SharedContentValidationResponse validate(
        String authorization,
        UUID sharedContentId,
        String shareToken,
        Long noteId
    ) {
        try {
            String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(VALIDATE_PATH)
                    .queryParam("sharedContentId", sharedContentId)
                    .queryParam("shareToken", shareToken)
                    .queryParam("contentType", CONTENT_TYPE_NOTE)
                    .queryParam("contentId", noteId)
                    .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new ExternalServiceException("Engagement 공유 검증 응답이 비어 있습니다");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode payload = root.has("data") ? root.get("data") : root;
            SharedContentValidationResponse response =
                objectMapper.treeToValue(payload, SharedContentValidationResponse.class);
            if (response == null) {
                throw new ExternalServiceException("Engagement 공유 검증 응답을 해석할 수 없습니다");
            }
            return response;
        } catch (RestClientException | IOException ex) {
            throw new ExternalServiceException("Engagement 공유 검증 호출에 실패했습니다", ex);
        }
    }
}
