package com.synapse.knowledge.search.client;

import com.synapse.knowledge.search.dto.client.PlatformMyTenantResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PlatformTenantClient {

    private static final String MY_TENANT_PATH = "/api/v1/tenants/me";

    private final RestClient restClient;

    public PlatformTenantClient(@Qualifier("platformRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public UUID getMyTenantId(String accessToken) {
        try {
            PlatformMyTenantResponse response = restClient.get()
                .uri(MY_TENANT_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(PlatformMyTenantResponse.class);

            if (response == null || response.id() == null) {
                throw new IllegalStateException("platform tenant 응답에 id가 없습니다");
            }

            return response.id();
        } catch (RestClientException ex) {
            throw new IllegalStateException("platform tenant 조회에 실패했습니다", ex);
        }
    }
}
