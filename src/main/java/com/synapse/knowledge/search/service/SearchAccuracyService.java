package com.synapse.knowledge.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.SearchAccuracyDetail;
import com.synapse.knowledge.search.dto.SearchAccuracyReport;
import com.synapse.knowledge.search.dto.SearchComparisonReport;
import com.synapse.knowledge.search.dto.SearchTestQuery;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.MrrCalculator;
import com.synapse.knowledge.search.service.support.NdcgCalculator;
import com.synapse.knowledge.search.service.support.PrecisionRecallCalculator;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import com.synapse.knowledge.search.service.support.SearchMode;
import com.synapse.knowledge.shared.NoteIdentityQueryPort;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchAccuracyService {

    private static final String SEARCH_TEST_QUERIES_RESOURCE = "search/accuracy/search-test-queries.json";

    private final SearchAccuracyBenchmarkSeeder benchmarkSeeder;
    private final NoteSearchRepository noteSearchRepository;
    private final SearchService searchService;
    private final LearningAiSearchClient learningAiSearchClient;
    private final NoteIdentityQueryPort noteIdentityQueryPort;
    private final PrecisionRecallCalculator precisionRecallCalculator;
    private final MrrCalculator mrrCalculator;
    private final NdcgCalculator ndcgCalculator;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;
    private final AtomicReference<SearchComparisonReport> latestReport = new AtomicReference<>();

    public synchronized SearchComparisonReport runAccuracyTest() {
        SearchAccuracyBenchmarkSeeder.BenchmarkContext context = benchmarkSeeder.seed();
        List<ResolvedSearchTestQuery> queries = resolveQueries(context.notesByBenchmarkId());
        SearchComparisonReport report = buildReport(context.toSearchIdentity(), queries);
        latestReport.set(report);
        return report;
    }

    public SearchComparisonReport getLatestReport() {
        SearchComparisonReport report = latestReport.get();
        return report == null ? runAccuracyTest() : report;
    }

    SearchComparisonReport buildReport(SearchIdentity identity, List<ResolvedSearchTestQuery> queries) {
        SearchAccuracyReport bm25Report = evaluate(SearchMode.BM25, identity, queries);
        SearchAccuracyReport semanticReport;
        boolean semanticAvailable = identity.canUseSemanticSearch();

        try {
            semanticReport = evaluate(SearchMode.SEMANTIC, identity, queries);
        } catch (Exception ex) {
            semanticAvailable = false;
            semanticReport = SearchAccuracyReport.empty(SearchMode.SEMANTIC, queries.size());
            log.warn("semantic accuracy evaluation failed", ex);
        }

        SearchAccuracyReport hybridReport = evaluate(SearchMode.HYBRID, identity, queries);

        return new SearchComparisonReport(
            Instant.now(),
            searchProperties.accuracy().datasetVersion(),
            semanticAvailable,
            bm25Report,
            semanticReport,
            hybridReport,
            buildImprovements(semanticAvailable, bm25Report, semanticReport, hybridReport)
        );
    }

    private SearchAccuracyReport evaluate(SearchMode mode, SearchIdentity identity, List<ResolvedSearchTestQuery> queries) {
        List<SearchAccuracyDetail> details = new ArrayList<>();
        List<Double> reciprocalRanks = new ArrayList<>();
        List<Double> ndcgScores = new ArrayList<>();
        double precisionSum = 0.0d;
        double recallSum = 0.0d;

        for (ResolvedSearchTestQuery query : queries) {
            List<Long> actualNoteIds = execute(mode, identity, query.query());
            Map<Long, Integer> relevanceByNoteId = query.toRelevanceMap();

            double precision = precisionRecallCalculator.precisionAtK(actualNoteIds, relevanceByNoteId.keySet(), searchProperties.accuracy().topK());
            double recall = precisionRecallCalculator.recallAtK(actualNoteIds, relevanceByNoteId.keySet(), searchProperties.accuracy().topK());
            double reciprocalRank = mrrCalculator.reciprocalRank(actualNoteIds, relevanceByNoteId.keySet());
            double ndcg = ndcgCalculator.ndcgAtK(actualNoteIds, relevanceByNoteId, searchProperties.accuracy().topK());

            precisionSum += precision;
            recallSum += recall;
            reciprocalRanks.add(reciprocalRank);
            ndcgScores.add(ndcg);
            details.add(new SearchAccuracyDetail(
                query.query(),
                query.expectedNoteIds(),
                actualNoteIds.stream().limit(searchProperties.accuracy().topK()).toList(),
                round(precision),
                round(recall),
                round(reciprocalRank),
                round(ndcg)
            ));
        }

        int queryCount = queries.size();
        return new SearchAccuracyReport(
            mode,
            queryCount,
            round(queryCount == 0 ? 0.0d : precisionSum / queryCount),
            round(queryCount == 0 ? 0.0d : recallSum / queryCount),
            round(mrrCalculator.meanReciprocalRank(reciprocalRanks)),
            round(ndcgCalculator.average(ndcgScores)),
            details
        );
    }

    private List<Long> execute(SearchMode mode, SearchIdentity identity, String query) {
        return switch (mode) {
            case BM25 -> noteSearchRepository.searchKeywordCandidates(
                    identity.userId(),
                    query,
                    searchProperties.accuracy().topK(),
                    null
                ).stream()
                .map(SearchCandidate::noteId)
                .filter(Objects::nonNull)
                .toList();
            case SEMANTIC -> {
                if (!identity.canUseSemanticSearch()) {
                    yield List.of();
                }
                yield learningAiSearchClient.searchSemantic(identity.semanticActorId(), query, searchProperties.accuracy().topK()).stream()
                    .map(hit -> noteIdentityQueryPort.findByExternalNoteId(hit.noteId()))
                    .flatMap(Optional::stream)
                    .map(NoteIdentityQueryPort.NoteIdentityView::noteId)
                    .distinct()
                    .toList();
            }
            case HYBRID -> searchService.hybridSearch(
                    identity,
                    new HybridSearchRequest(query, searchProperties.accuracy().topK(), null)
                ).results().stream()
                .map(UnifiedSearchResultResponse::noteId)
                .filter(Objects::nonNull)
                .toList();
        };
    }

    private List<ResolvedSearchTestQuery> resolveQueries(Map<String, SearchAccuracyBenchmarkSeeder.SeededNote> notesByBenchmarkId) {
        return loadSearchTestQueries().stream()
            .map(seed -> toResolvedQuery(seed, notesByBenchmarkId))
            .toList();
    }

    private ResolvedSearchTestQuery toResolvedQuery(
        SearchTestQuerySeed seed,
        Map<String, SearchAccuracyBenchmarkSeeder.SeededNote> notesByBenchmarkId
    ) {
        if (seed.expectedBenchmarkIds().size() != seed.relevanceScores().size()) {
            throw new IllegalStateException("expectedBenchmarkIds and relevanceScores size mismatch for query: " + seed.query());
        }

        List<Long> expectedNoteIds = seed.expectedBenchmarkIds().stream()
            .map(benchmarkId -> Optional.ofNullable(notesByBenchmarkId.get(benchmarkId))
                .orElseThrow(() -> new IllegalStateException("benchmark note not found: " + benchmarkId))
                .noteId())
            .toList();

        return new ResolvedSearchTestQuery(seed.query(), expectedNoteIds, seed.relevanceScores());
    }

    private List<SearchTestQuerySeed> loadSearchTestQueries() {
        try (InputStream inputStream = new ClassPathResource(SEARCH_TEST_QUERIES_RESOURCE).getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<SearchTestQuerySeed>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load search test queries resource: " + SEARCH_TEST_QUERIES_RESOURCE, ex);
        }
    }

    private List<String> buildImprovements(
        boolean semanticAvailable,
        SearchAccuracyReport bm25Report,
        SearchAccuracyReport semanticReport,
        SearchAccuracyReport hybridReport
    ) {
        List<String> improvements = new ArrayList<>();

        if (!semanticAvailable) {
            improvements.add("semantic 검색 측정이 실패했습니다. learning-ai 연결 상태와 semantic actor 설정을 점검하세요.");
        }
        if (bm25Report.precisionAt10() < 0.6d) {
            improvements.add("BM25 Precision@10이 목표 미달입니다. title/content/tags field boost와 nori analyzer 구성을 재조정하세요.");
        }
        if (hybridReport.mrr() < 0.7d) {
            improvements.add("Hybrid MRR이 목표 미달입니다. candidate multiplier, RRF k, semantic threshold를 함께 튜닝하세요.");
        }
        if (hybridReport.ndcgAt10() < bm25Report.ndcgAt10()) {
            improvements.add("Hybrid NDCG가 BM25보다 낮습니다. semantic 결과의 top-k 품질과 note_id 매핑 정합성을 재검증하세요.");
        }
        if (semanticAvailable && semanticReport.mrr() == 0.0d) {
            improvements.add("semantic 결과가 모두 비어 있습니다. embedding 색인 상태와 threshold 값을 확인하세요.");
        }
        if (improvements.isEmpty()) {
            improvements.add("현재 benchmark 기준으로 즉시 튜닝이 필요한 급한 정확도 저하는 보이지 않습니다.");
        }

        return improvements;
    }

    private double round(double value) {
        return Math.round(value * 10_000d) / 10_000d;
    }

    record SearchTestQuerySeed(
        String query,
        List<String> expectedBenchmarkIds,
        List<Integer> relevanceScores
    ) {
    }

    static record ResolvedSearchTestQuery(
        String query,
        List<Long> expectedNoteIds,
        List<Integer> relevanceScores
    ) {
        Map<Long, Integer> toRelevanceMap() {
            Map<Long, Integer> relevanceByNoteId = new LinkedHashMap<>();
            for (int i = 0; i < expectedNoteIds.size(); i++) {
                relevanceByNoteId.put(expectedNoteIds.get(i), relevanceScores.get(i));
            }
            return relevanceByNoteId;
        }

        SearchTestQuery toDto() {
            return new SearchTestQuery(query, expectedNoteIds, relevanceScores);
        }
    }
}
