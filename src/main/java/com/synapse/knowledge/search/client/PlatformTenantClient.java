package com.synapse.knowledge.search.client;

import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.client.PlatformMyTenantResponse;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class PlatformTenantClient {

    private static final String MY_TENANT_PATH = "/api/v1/tenants/me";
    static final String DEFAULT_LOOPBACK_FALLBACK_BASE_URL = "http://platform-svc:8081";

    private final RestClient primaryRestClient;
    private final RestClient fallbackRestClient;

    @Autowired
    public PlatformTenantClient(
        @Qualifier("platformRestClient") RestClient restClient,
        RestClient.Builder builder,
        SearchProperties properties
    ) {
        this(restClient, builder, properties, DEFAULT_LOOPBACK_FALLBACK_BASE_URL);
    }

    PlatformTenantClient(
        RestClient restClient,
        RestClient.Builder builder,
        SearchProperties properties,
        String fallbackBaseUrl
    ) {
        this.primaryRestClient = restClient;
        this.fallbackRestClient = shouldUseLoopbackFallback(properties.platform().baseUrl())
            ? buildRestClient(builder, fallbackBaseUrl, properties.platform().timeout())
            : null;
    }

    public UUID getMyTenantId(String accessToken) {
        try {
            return fetchTenantId(primaryRestClient, accessToken);
        } catch (RestClientException ex) {
            if (fallbackRestClient != null && ex instanceof ResourceAccessException) {
                try {
                    return fetchTenantId(fallbackRestClient, accessToken);
                } catch (RestClientException fallbackEx) {
                    ex.addSuppressed(fallbackEx);
                }
            }
            throw new IllegalStateException("platform tenant 조회에 실패했습니다", ex);
        }
    }

    private UUID fetchTenantId(RestClient restClient, String accessToken) {
        PlatformMyTenantResponse response = restClient.get()
            .uri(MY_TENANT_PATH)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .retrieve()
            .body(PlatformMyTenantResponse.class);

        if (response == null || response.id() == null) {
            throw new IllegalStateException("platform tenant 응답에 id가 없습니다");
        }

        return response.id();
    }

    private boolean shouldUseLoopbackFallback(String baseUrl) {
        URI uri = URI.create(baseUrl);
        String host = uri.getHost();
        return "localhost".equalsIgnoreCase(host)
            || "127.0.0.1".equals(host)
            || "::1".equals(host)
            || "[::1]".equals(host);
    }

    private RestClient buildRestClient(RestClient.Builder builder, String baseUrl, Duration timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return builder.clone()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }
}
