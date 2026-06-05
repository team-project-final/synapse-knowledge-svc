package com.synapse.knowledge.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import com.synapse.knowledge.search.service.consumer.NoteSearchKafkaConsumer;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(properties = {
    "spring.kafka.consumer.group-id=knowledge-search-indexer-e2e",
    "spring.kafka.consumer.auto-offset-reset=latest",
    "search.ai.timeout=200ms"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchElasticsearchIntegrationTest {

    private static final String INDEX_NAME = "notes-v1";

    @Container
    static final ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:9.2.1")
    )
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("elasticsearch/elasticsearch-plugins.yml"),
            "/usr/share/elasticsearch/config/elasticsearch-plugins.yml"
        );

    @DynamicPropertySource
    static void registerElasticProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchAccuracyService searchAccuracyService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private com.synapse.knowledge.search.repository.ElasticsearchNoteSearchRepository elasticsearchNoteSearchRepository;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @MockitoBean
    private LearningAiSearchClient learningAiSearchClient;

    @BeforeEach
    void setUp() throws IOException {
        if (!elasticsearch.isRunning()) {
            elasticsearch.start();
        }
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
        deleteIndexIfExists();
        waitForSearchListenerReady();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (!elasticsearch.isRunning()) {
            resetIndexEnsuredFlag();
            return;
        }
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
        deleteIndexIfExists();
    }

    @DisplayName("한국어 복합명사 노트를 저장하면 Nori 분해 검색과 하이라이트가 동작한다")
    @Test
    @Order(1)
    void search_koreanCompoundNounNote_shouldWorkWithNoriAnalysisAndHighlight() throws IOException {
        // Given
        Long ownerId = 100L;
        noteService.create(
            ownerId,
            new NoteCreateRequest(
                "tenant1",
                "가곡역 여행기",
                "부산 가곡역 주변 산책 기록입니다.",
                List.of("한국어", "여행")
            )
        );
        noteService.create(
            200L,
            new NoteCreateRequest(
                "tenant1",
                "가곡역 타인 노트",
                "다른 사용자의 노트라 검색되면 안 됩니다.",
                List.of("한국어")
            )
        );

        // When
        SearchPageResponse response = waitForResults(ownerId, "가곡", null, 20);

        // Then
        assertThat(analyze("가곡역")).contains("가곡역", "가곡", "역");
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).title()).isEqualTo("가곡역 여행기");
        assertThat(response.results().get(0).highlights()).isNotEmpty();
        assertThat(response.totalCount()).isEqualTo(1L);
    }

    @DisplayName("태그 필터를 함께 주면 조건에 맞는 노트만 반환한다")
    @Test
    @Order(2)
    void search_withTagFilter_shouldReturnOnlyMatchingNotes() {
        // Given
        Long ownerId = 300L;
        noteService.create(ownerId, new NoteCreateRequest("tenant1", "스프링 검색", "spring boot elasticsearch", List.of("backend", "search")));
        noteService.create(ownerId, new NoteCreateRequest("tenant1", "스프링 인증", "spring security jwt", List.of("backend", "auth")));

        // When
        SearchPageResponse response = waitForResults(ownerId, "스프링", List.of("search"), 20);

        // Then
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).title()).isEqualTo("스프링 검색");
    }

    @DisplayName("시맨틱 결과가 함께 오면 RRF 점수순으로 병합된다")
    @Test
    @Order(3)
    void hybridSearch_withSemanticResults_shouldMergeByRrfScore() {
        // Given
        Long ownerId = 500L;
        String semanticActorId = UUID.randomUUID().toString();
        Long springNoteId = noteService.create(
            ownerId,
            new NoteCreateRequest("tenant1", "스프링 시큐리티", "spring security jwt resource server", List.of("backend"))
        ).id();
        Long elasticNoteId = noteService.create(
            ownerId,
            new NoteCreateRequest("tenant1", "엘라스틱서치", "bm25 search nori analyzer", List.of("search"))
        ).id();
        UUID springExternalNoteId = noteIdentityMapRepository.findById(springNoteId)
            .orElseThrow(() -> new IllegalStateException("missing note identity mapping: " + springNoteId))
            .getExternalNoteId();
        UUID elasticExternalNoteId = noteIdentityMapRepository.findById(elasticNoteId)
            .orElseThrow(() -> new IllegalStateException("missing note identity mapping: " + elasticNoteId))
            .getExternalNoteId();
        given(learningAiSearchClient.searchSemantic(semanticActorId, "스프링", 30)).willReturn(List.of(
            new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), springExternalNoteId, "의미상 관련", 0.98f),
            new LearningAiSearchClient.LearningAiSemanticHit(UUID.randomUUID(), elasticExternalNoteId, "의미상 관련", 0.95f)
        ));

        // When
        waitForResults(ownerId, "스프링", null, 20);
        HybridSearchResponse response = searchService.hybridSearch(
            new SearchIdentity(ownerId, semanticActorId),
            new HybridSearchRequest("스프링", 10, null)
        );

        // Then
        assertThat(response.results()).isNotEmpty();
        assertThat(response.results().get(0).noteId()).isEqualTo(springNoteId);
        assertThat(response.results().get(0).semanticScore()).isNotNull();
        assertThat(response.semanticFallback()).isFalse();
    }

    @DisplayName("시맨틱 검색이 타임아웃되면 BM25 결과만 반환한다")
    @Test
    @Order(4)
    void hybridSearch_semanticTimeout_shouldReturnBm25Fallback() {
        // Given
        Long ownerId = 700L;
        String semanticActorId = UUID.randomUUID().toString();
        Long springNoteId = noteService.create(
            ownerId,
            new NoteCreateRequest("tenant1", "스프링 타임아웃", "spring timeout fallback search", List.of("backend", "search"))
        ).id();
        given(learningAiSearchClient.searchSemantic(semanticActorId, "스프링", 30))
            .willAnswer(invocation -> {
                sleep(Duration.ofMillis(400));
                return List.of(new LearningAiSearchClient.LearningAiSemanticHit(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "느린 semantic 응답",
                    0.91f
                ));
            });

        // When
        waitForResults(ownerId, "스프링", null, 20);
        HybridSearchResponse response = searchService.hybridSearch(
            new SearchIdentity(ownerId, semanticActorId),
            new HybridSearchRequest("스프링", 10, null)
        );

        // Then
        assertThat(response.results()).isNotEmpty();
        assertThat(response.results().get(0).noteId()).isEqualTo(springNoteId);
        assertThat(response.results().get(0).semanticScore()).isNull();
        assertThat(response.semanticFallback()).isTrue();
    }

    @DisplayName("benchmark 세트를 실행하면 세 모드 리포트를 생성한다")
    @Test
    @Order(5)
    void accuracyReport_runBenchmarkSet_shouldGenerateThreeModeReport() {
        given(learningAiSearchClient.searchSemantic(anyString(), anyString(), anyInt())).willReturn(List.of());

        var report = searchAccuracyService.runAccuracyTest();

        assertThat(report.datasetVersion()).isEqualTo("test-v1");
        assertThat(report.bm25().queryCount()).isGreaterThanOrEqualTo(50);
        assertThat(report.semantic().queryCount()).isGreaterThanOrEqualTo(50);
        assertThat(report.hybrid().queryCount()).isGreaterThanOrEqualTo(50);
        assertThat(report.improvements()).isNotEmpty();
    }

    @DisplayName("Elasticsearch가 중단되면 검색 요청은 실패한다")
    @Test
    @Order(6)
    void search_elasticsearchUnavailable_shouldThrowException() {
        // Given
        Long ownerId = 900L;
        noteService.create(
            ownerId,
            new NoteCreateRequest("tenant1", "Elasticsearch 다운 테스트", "cluster unavailable failure path", List.of("ops"))
        );
        waitForResults(ownerId, "Elasticsearch", null, 20);
        elasticsearch.stop();
        resetIndexEnsuredFlag();

        // When & Then
        assertThatThrownBy(() -> searchService.search(ownerId, new SearchRequest("Elasticsearch", null, 20, null)))
            .isInstanceOf(RuntimeException.class);
    }

    private SearchPageResponse waitForResults(Long userId, String query, List<String> tags, int limit) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(20));
        RuntimeException lastRetryableFailure = null;

        while (Instant.now().isBefore(deadline)) {
            try {
                SearchPageResponse response = searchService.search(userId, new SearchRequest(query, null, limit, tags));
                if (!response.results().isEmpty()) {
                    return response;
                }
            } catch (RuntimeException ex) {
                if (!isRetryableSearchFailure(ex)) {
                    throw ex;
                }
                lastRetryableFailure = ex;
            }

            sleepBriefly();
        }

        if (lastRetryableFailure != null) {
            throw lastRetryableFailure;
        }
        return searchService.search(userId, new SearchRequest(query, null, limit, tags));
    }

    private List<String> analyze(String text) throws IOException {
        return elasticsearchClient.indices().analyze(analyze -> analyze
                .index(INDEX_NAME)
                .analyzer("korean_nori")
                .text(text))
            .tokens().stream()
            .map(token -> token.token())
            .toList();
    }

    private void deleteIndexIfExists() throws IOException {
        boolean exists = elasticsearchClient.indices()
            .exists(request -> request.index(INDEX_NAME))
            .value();

        if (!exists) {
            resetIndexEnsuredFlag();
            return;
        }

        try {
            elasticsearchClient.indices().delete(delete -> delete.index(INDEX_NAME));
        } catch (ElasticsearchException ex) {
            if (ex.status() == 404) {
                resetIndexEnsuredFlag();
                return;
            }
            throw ex;
        }

        waitUntilIndexDeleted();
        resetIndexEnsuredFlag();
    }

    private boolean isRetryableSearchFailure(RuntimeException ex) {
        Throwable cause = ex;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && (
                message.contains("index_not_found_exception")
                    || message.contains("no_shard_available_action_exception")
                    || message.contains("resource_already_exists_exception")
            )) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void sleepBriefly() {
        sleep(Duration.ofMillis(250));
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Elasticsearch indexing wait interrupted", ex);
        }
    }

    private void waitUntilIndexDeleted() throws IOException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            boolean exists = elasticsearchClient.indices()
                .exists(request -> request.index(INDEX_NAME))
                .value();
            if (!exists) {
                return;
            }
            sleepBriefly();
        }
    }

    private void resetIndexEnsuredFlag() {
        ReflectionTestUtils.setField(elasticsearchNoteSearchRepository, "indexEnsured", false);
    }

    private void waitForSearchListenerReady() {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(20));

        while (Instant.now().isBefore(deadline)) {
            MessageListenerContainer container =
                kafkaListenerEndpointRegistry.getListenerContainer(NoteSearchKafkaConsumer.LISTENER_ID);
            if (container != null && container.isRunning() && hasAssignedPartitions(container)) {
                return;
            }
            sleepBriefly();
        }

        throw new IllegalStateException("search Kafka listener was not ready within PT20S");
    }

    private boolean hasAssignedPartitions(MessageListenerContainer container) {
        Object assignedPartitions = ReflectionTestUtils.invokeMethod(container, "getAssignedPartitions");
        return assignedPartitions instanceof Collection<?> partitions && !partitions.isEmpty();
    }
}
