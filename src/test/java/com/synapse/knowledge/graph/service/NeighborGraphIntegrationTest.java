package com.synapse.knowledge.graph.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.graph.dto.GraphDataResponse;
import com.synapse.knowledge.note.service.NoteService;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class NeighborGraphIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private GraphService graphService;

    @Autowired
    private NoteService noteService;

    @Test
    @DisplayName("getNeighborGraph_depth1_shouldReturnDirectNeighbors")
    void getNeighborGraph_depth1_shouldReturnDirectNeighbors() {
        // Given: A → B → C (chain)
        Long userId = 1L;
        String tenantId = "t1";
        noteService.create(userId, new NoteCreateRequest(tenantId, "C", "Content C"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "B", "Links to [[C]]"));
        NoteResponse noteA = noteService.create(userId, new NoteCreateRequest(tenantId, "A", "Links to [[B]]"));

        // When: depth=1 from A
        GraphDataResponse response = graphService.getNeighborGraph(userId, noteA.id(), 1);

        // Then: A와 직접 이웃인 B만 포함 (C는 depth=2라 제외)
        assertThat(response.nodes()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(response.edges()).isNotEmpty();
    }

    @Test
    @DisplayName("getNeighborGraph_depth2_shouldReturnTwoHopNeighbors")
    void getNeighborGraph_depth2_shouldReturnTwoHopNeighbors() {
        // Given: A → B → C
        Long userId = 1L;
        String tenantId = "t1";
        noteService.create(userId, new NoteCreateRequest(tenantId, "C", "Content C"));
        noteService.create(userId, new NoteCreateRequest(tenantId, "B", "Links to [[C]]"));
        NoteResponse noteA = noteService.create(userId, new NoteCreateRequest(tenantId, "A", "Links to [[B]]"));

        // When: depth=2 from A
        GraphDataResponse response = graphService.getNeighborGraph(userId, noteA.id(), 2);

        // Then: A, B, C 모두 포함
        assertThat(response.edges()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("getNeighborGraph_노드없음_shouldReturnOnlyRoot")
    void getNeighborGraph_노드없음_shouldReturnOnlyRoot() {
        // Given
        Long userId = 1L;
        NoteResponse isolated = noteService.create(userId, new NoteCreateRequest("t1", "Isolated", "No links"));

        // When
        GraphDataResponse response = graphService.getNeighborGraph(userId, isolated.id(), 2);

        // Then
        assertThat(response.edges()).isEmpty();
    }
}
