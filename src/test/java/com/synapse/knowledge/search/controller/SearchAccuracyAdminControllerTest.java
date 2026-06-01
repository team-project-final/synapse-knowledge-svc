package com.synapse.knowledge.search.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.synapse.knowledge.search.dto.SearchAccuracyReport;
import com.synapse.knowledge.search.dto.SearchComparisonReport;
import com.synapse.knowledge.search.service.SearchAccuracyService;
import com.synapse.knowledge.search.service.support.SearchMode;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SearchAccuracyAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchAccuracyService searchAccuracyService;

    @Test
    @DisplayName("accuracyTest_인증없음_shouldReturn401")
    void accuracyTest_인증없음_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/search/accuracy-test"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("accuracyTest_관리자권한없음_shouldReturn403")
    void accuracyTest_관리자권한없음_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/search/accuracy-test")
                .with(jwt().jwt(jwt -> jwt.subject("100").claim("userId", 100L))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("accuracyReport_관리자권한있음_shouldReturn200")
    void accuracyReport_관리자권한있음_shouldReturn200() throws Exception {
        SearchComparisonReport report = new SearchComparisonReport(
            Instant.parse("2026-06-01T00:00:00Z"),
            "test-v1",
            true,
            new SearchAccuracyReport(SearchMode.BM25, 50, 0.82d, 0.74d, 0.86d, 0.88d, List.of()),
            new SearchAccuracyReport(SearchMode.SEMANTIC, 50, 0.80d, 0.70d, 0.83d, 0.85d, List.of()),
            new SearchAccuracyReport(SearchMode.HYBRID, 50, 0.88d, 0.78d, 0.90d, 0.92d, List.of()),
            List.of("Hybrid MRR이 높아 현재 조합을 기본값으로 유지합니다.")
        );
        given(searchAccuracyService.getLatestReport()).willReturn(report);

        mockMvc.perform(get("/api/v1/admin/search/accuracy-report")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    .jwt(jwt -> jwt.subject("1").claim("userId", 1L).claim("roles", List.of("ADMIN")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.datasetVersion").value("test-v1"))
            .andExpect(jsonPath("$.data.hybrid.queryCount").value(50))
            .andExpect(jsonPath("$.data.semanticAvailable").value(true));
    }
}
