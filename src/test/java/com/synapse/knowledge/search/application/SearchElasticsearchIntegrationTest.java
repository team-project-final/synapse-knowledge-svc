package com.synapse.knowledge.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.synapse.knowledge.note.application.NoteService;
import com.synapse.knowledge.note.domain.NoteRepository;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.search.dto.SearchPageResponse;
import com.synapse.knowledge.search.dto.SearchRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SearchElasticsearchIntegrationTest {

    private static final String INDEX_NAME = "notes-v1";

    @Container
    static final GenericContainer<?> elasticsearch = new GenericContainer<>(
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.19.6")
    )
        .withExposedPorts(9200)
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
        .withEnv("ingest.geoip.downloader.enabled", "false")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("elasticsearch/elasticsearch-plugins.yml"),
            "/usr/share/elasticsearch/config/elasticsearch-plugins.yml"
        )
        .waitingFor(Wait.forHttp("/").forStatusCode(200))
        .withStartupTimeout(Duration.ofMinutes(4));

    @DynamicPropertySource
    static void registerElasticProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
    }

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @BeforeEach
    void setUp() throws IOException {
        noteRepository.deleteAll();
        deleteIndexIfExists();
    }

    @AfterEach
    void tearDown() throws IOException {
        noteRepository.deleteAll();
        deleteIndexIfExists();
    }

    @DisplayName("search_한국어복합명사노트를저장하면_shouldNori분해검색과하이라이트가동작")
    @Test
    void search_한국어복합명사노트를저장하면_shouldNori분해검색과하이라이트가동작() throws IOException {
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

    @DisplayName("search_태그필터를함께주면_should조건에맞는노트만반환")
    @Test
    void search_태그필터를함께주면_should조건에맞는노트만반환() {
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

    private SearchPageResponse waitForResults(Long userId, String query, List<String> tags, int limit) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(20));

        while (Instant.now().isBefore(deadline)) {
            SearchPageResponse response = searchService.search(userId, new SearchRequest(query, null, limit, tags));
            if (!response.results().isEmpty()) {
                return response;
            }

            try {
                Thread.sleep(250L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Elasticsearch indexing wait interrupted", ex);
            }
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
            return;
        }

        try {
            elasticsearchClient.indices().delete(delete -> delete.index(INDEX_NAME));
        } catch (ElasticsearchException ex) {
            if (ex.status() == 404) {
                return;
            }
            throw ex;
        }
    }
}
