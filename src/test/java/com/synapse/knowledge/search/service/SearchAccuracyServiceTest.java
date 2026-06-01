package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchComparisonReport;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.MrrCalculator;
import com.synapse.knowledge.search.service.support.NdcgCalculator;
import com.synapse.knowledge.search.service.support.PrecisionRecallCalculator;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import com.synapse.knowledge.shared.NoteIdentityQueryPort;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchAccuracyServiceTest {

    @Mock
    private SearchAccuracyBenchmarkSeeder benchmarkSeeder;

    @Mock
    private NoteSearchRepository noteSearchRepository;

    @Mock
    private SearchService searchService;

    @Mock
    private LearningAiSearchClient learningAiSearchClient;

    @Mock
    private NoteIdentityQueryPort noteIdentityQueryPort;

    private final SearchProperties searchProperties = new SearchProperties(
        new SearchProperties.Ai("http://localhost:8090", Duration.ofSeconds(3), 0.7d),
        new SearchProperties.Hybrid(60, 3),
        new SearchProperties.Accuracy("test-v1", 910000L, "benchmark-search", "11111111-1111-1111-1111-111111111111", 10, Duration.ofSeconds(5))
    );

    @Test
    @DisplayName("buildReport_세모드결과를계산하면_should비교리포트를반환")
    void buildReport_세모드결과를계산하면_should비교리포트를반환() {
        SearchAccuracyService service = new SearchAccuracyService(
            benchmarkSeeder,
            noteSearchRepository,
            searchService,
            learningAiSearchClient,
            noteIdentityQueryPort,
            new PrecisionRecallCalculator(),
            new MrrCalculator(),
            new NdcgCalculator(),
            searchProperties,
            new ObjectMapper()
        );
        SearchIdentity identity = new SearchIdentity(910000L, searchProperties.accuracy().semanticActorId());
        UUID semanticNoteId = UUID.randomUUID();

        List<SearchAccuracyService.ResolvedSearchTestQuery> queries = List.of(
            new SearchAccuracyService.ResolvedSearchTestQuery("spring security", List.of(1L), List.of(2)),
            new SearchAccuracyService.ResolvedSearchTestQuery("hybrid rrf", List.of(2L), List.of(2))
        );

        given(noteSearchRepository.searchKeywordCandidates(910000L, "spring security", 10, null))
            .willReturn(List.of(new SearchCandidate(1L, semanticNoteId, "note1", List.of(), "snippet", 1.0f, null)));
        given(noteSearchRepository.searchKeywordCandidates(910000L, "hybrid rrf", 10, null))
            .willReturn(List.of(new SearchCandidate(2L, semanticNoteId, "note2", List.of(), "snippet", 1.0f, null)));

        given(learningAiSearchClient.searchSemantic(anyString(), anyString(), anyInt()))
            .willAnswer(invocation -> {
                String query = invocation.getArgument(1, String.class);
                if ("spring security".equals(query)) {
                    return List.of(new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), semanticNoteId, "content", 0.9f));
                }
                return List.of(new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), UUID.randomUUID(), "content", 0.8f));
            });
        given(noteIdentityQueryPort.findByExternalNoteId(semanticNoteId))
            .willReturn(Optional.of(new NoteIdentityQueryPort.NoteIdentityView(1L, semanticNoteId, "note1")));

        given(searchService.hybridSearch(identity, new com.synapse.knowledge.search.dto.HybridSearchRequest("spring security", 10, null)))
            .willReturn(HybridSearchResponse.of(
                List.of(new UnifiedSearchResultResponse(1L, "note1", List.of(), "snippet", 1.0f, 0.9f, 0.03f)),
                30L,
                false
            ));
        given(searchService.hybridSearch(identity, new com.synapse.knowledge.search.dto.HybridSearchRequest("hybrid rrf", 10, null)))
            .willReturn(HybridSearchResponse.of(
                List.of(new UnifiedSearchResultResponse(2L, "note2", List.of(), "snippet", 1.0f, null, 0.02f)),
                20L,
                true
            ));

        SearchComparisonReport report = service.buildReport(identity, queries);

        assertThat(report.datasetVersion()).isEqualTo("test-v1");
        assertThat(report.bm25().queryCount()).isEqualTo(2);
        assertThat(report.semantic().queryCount()).isEqualTo(2);
        assertThat(report.hybrid().queryCount()).isEqualTo(2);
        assertThat(report.bm25().mrr()).isGreaterThan(0.0d);
        assertThat(report.improvements()).isNotEmpty();
    }
}
