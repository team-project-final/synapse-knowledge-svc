package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.PlatformTenantClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemanticTenantResolver {

    private final PlatformTenantClient platformTenantClient;

    public String resolve(SearchIdentity identity) {
        if (identity.hasSemanticTenantId()) {
            return identity.semanticTenantId();
        }
        if (identity.hasAccessToken()) {
            return platformTenantClient.getMyTenantId(identity.accessToken()).toString();
        }
        throw new IllegalStateException("semantic 검색에는 tenant 해석 가능한 인증 정보가 필요합니다");
    }
}
