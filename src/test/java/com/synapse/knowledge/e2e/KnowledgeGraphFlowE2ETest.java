package com.synapse.knowledge.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 노트/그래프 도메인의 핵심 플로우 E2E (H2 기반, ES/Kafka 불필요).
 *
 * <p>MockMvc로 실제 HTTP 엔드포인트 → 필터 체인 → 컨트롤러 → 실서비스 → DB 까지 통과시킨다.
 * TASK Step 8 / #47 의 시나리오를 다룬다:
 * <ul>
 *   <li>S1 노트 생성 → 위키링크 파싱 → 백링크/아웃링크</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class KnowledgeGraphFlowE2ETest {

    private static final long USER = 100L;
    private static final long OTHER_USER = 200L;
    private static final String TENANT = "tenant-e2e";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("createNoteWithWikiLink_위키링크포함노트_shouldPersistBacklinkAndOutlink")
    void createNoteWithWikiLink_위키링크포함노트_shouldPersistBacklinkAndOutlink() throws Exception {
        // Given: 위키링크 대상이 될 노트("Spring")를 먼저 생성
        long targetId = createNote("Spring", "스프링 타깃 노트 본문", List.of());

        // When: [[Spring]] 위키링크를 포함한 소스 노트를 생성
        long sourceId = createNote("Note A", "I love [[Spring]]", List.of());

        // Then: 소스의 아웃링크에 타깃이, 타깃의 백링크에 소스가 잡힌다
        mockMvc.perform(get("/api/v1/notes/{id}/outlinks", sourceId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].title").value("Spring"));

        mockMvc.perform(get("/api/v1/notes/{id}/backlinks", targetId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].title").value("Note A"));
    }

    @Test
    @DisplayName("backlinkFlow_위키링크로연결된노트_shouldReturnGraphNodesAndEdges")
    void backlinkFlow_위키링크로연결된노트_shouldReturnGraphNodesAndEdges() throws Exception {
        // Given: [[Spring]] 위키링크로 연결된 노트 두 개
        long targetId = createNote("Spring", "그래프 타깃 노트", List.of());
        long sourceId = createNote("Note A", "I love [[Spring]]", List.of());

        // When: D3.js 호환 그래프 데이터 조회
        MvcResult result = mockMvc.perform(get("/api/v1/graph/data").with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        // Then: 두 노트가 노드로, 위키링크가 엣지로 반환된다
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");

        List<String> titles = new ArrayList<>();
        data.path("nodes").forEach(node -> titles.add(node.path("title").asText()));
        assertThat(titles).contains("Spring", "Note A");

        boolean wikiLinkEdgeFound = false;
        for (JsonNode edge : data.path("edges")) {
            if (edge.path("source").asLong() == sourceId
                && edge.path("target").asLong() == targetId
                && "wikilink".equals(edge.path("type").asText())) {
                wikiLinkEdgeFound = true;
            }
        }
        assertThat(wikiLinkEdgeFound).isTrue();
    }

    @Test
    @DisplayName("tagFilterAndAutocomplete_태그추가_shouldFilterAndAutocompleteOwnedTags")
    void tagFilterAndAutocomplete_태그추가_shouldFilterAndAutocompleteOwnedTags() throws Exception {
        // Given: 태그가 달린 노트 세 개 (java 2건, python 1건)
        createNote("스프링 노트", "스프링 본문", List.of("spring", "java"));
        createNote("파이썬 노트", "파이썬 본문", List.of("python"));
        createNote("자바 노트", "자바 본문", List.of("java"));

        // When & Then 1: tag=java 필터 시 java 태그를 가진 노트만 반환
        MvcResult filtered = mockMvc.perform(get("/api/v1/notes")
                .param("tag", "java")
                .with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        JsonNode page = objectMapper.readTree(filtered.getResponse().getContentAsString()).path("data");
        assertThat(page.path("totalElements").asInt()).isEqualTo(2);
        for (JsonNode note : page.path("content")) {
            List<String> tags = new ArrayList<>();
            note.path("tags").forEach(tag -> tags.add(tag.asText()));
            assertThat(tags).contains("java");
        }

        // When & Then 2: 'ja' 접두사 자동완성 시 java 태그를 빈도수와 함께 반환
        mockMvc.perform(get("/api/v1/tags/autocomplete")
                .param("q", "ja")
                .with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].tag").value("java"))
            .andExpect(jsonPath("$.data[0].count").value(2));
    }

    @Test
    @DisplayName("editThenRestoreVersion_노트수정_shouldSnapshotVersionAndRestore")
    void editThenRestoreVersion_노트수정_shouldSnapshotVersionAndRestore() throws Exception {
        // Given: 노트를 생성한 뒤 수정하면 이전 상태가 버전 1로 스냅샷된다
        long noteId = createNote("원본 제목", "원본 내용", List.of());
        updateNote(noteId, "수정 제목", "수정 내용");

        // When & Then 1: 버전 목록에 원본이 버전 1로 남는다
        mockMvc.perform(get("/api/v1/notes/{id}/versions", noteId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].versionNo").value(1))
            .andExpect(jsonPath("$.data[0].title").value("원본 제목"));

        // When & Then 2: 버전 상세 조회 시 원본 제목/내용을 반환
        mockMvc.perform(get("/api/v1/notes/{id}/versions/{versionNo}", noteId, 1).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionNo").value(1))
            .andExpect(jsonPath("$.data.title").value("원본 제목"))
            .andExpect(jsonPath("$.data.contentMd").value("원본 내용"));

        // When & Then 3: 버전 1로 복원하면 노트가 원본 상태로 돌아간다
        mockMvc.perform(post("/api/v1/notes/{id}/versions/{versionNo}/restore", noteId, 1).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("원본 제목"));

        mockMvc.perform(get("/api/v1/notes/{id}", noteId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("원본 제목"));
    }

    @Test
    @DisplayName("noteAccessIsolation_타인노트접근_shouldDenyAndExcludeFromList")
    void noteAccessIsolation_타인노트접근_shouldDenyAndExcludeFromList() throws Exception {
        // Given: 사용자 A(USER)가 노트를 생성
        long ownerNoteId = createNote("A의 노트", "소유자 전용 본문", List.of());

        // When & Then 1: 다른 사용자(B)가 A의 노트 상세를 조회하면 403
        mockMvc.perform(get("/api/v1/notes/{id}", ownerNoteId).with(E2eJwtHelper.user(OTHER_USER)))
            .andExpect(status().isForbidden());

        // When & Then 2: B의 노트 목록에는 A의 노트가 포함되지 않는다 (소유자 격리)
        mockMvc.perform(get("/api/v1/notes").with(E2eJwtHelper.user(OTHER_USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("deletedNoteLookup_삭제된노트조회_shouldReturn404")
    void deletedNoteLookup_삭제된노트조회_shouldReturn404() throws Exception {
        // Given: 사용자가 조회 가능한 노트를 생성
        long noteId = createNote("삭제 대상", "삭제될 본문", List.of());

        // When: 노트를 soft delete
        mockMvc.perform(delete("/api/v1/notes/{id}", noteId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // Then: 같은 노트 상세 조회는 404로 응답
        mockMvc.perform(get("/api/v1/notes/{id}", noteId).with(E2eJwtHelper.user(USER)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("KNOW-404"));
    }

    /** 노트를 수정한다 (이전 상태가 버전으로 스냅샷됨). */
    private void updateNote(long id, String title, String contentMd) throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(TENANT, title, contentMd, List.of());
        mockMvc.perform(patch("/api/v1/notes/{id}", id)
                .with(E2eJwtHelper.user(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    /** 노트를 생성하고 생성된 노트의 id를 반환한다. */
    private long createNote(String title, String contentMd, List<String> tags) throws Exception {
        NoteCreateRequest request = new NoteCreateRequest(TENANT, title, contentMd, tags);
        MvcResult result = mockMvc.perform(post("/api/v1/notes")
                .with(E2eJwtHelper.user(USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
