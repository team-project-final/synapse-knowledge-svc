package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.dto.PopularTagResponse;
import com.synapse.knowledge.note.dto.TagAutoCompleteResponse;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TagIntegrationTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private TagService tagService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
        var popularKeys = redisTemplate.keys("tags:popular*");
        if (popularKeys != null && !popularKeys.isEmpty()) {
            redisTemplate.delete(popularKeys);
        }
    }

    @AfterEach
    void tearDown() {
        noteIdentityMapRepository.deleteAll();
        noteRepository.deleteAll();
    }

    @Test
    @DisplayName("저장된 태그로 자동완성 요청 시 prefix에 매칭되는 태그를 반환한다")
    void autocomplete_existingTags_shouldReturnMatchingTags() {
        Long userId = 1L;
        noteService.create(userId, new NoteCreateRequest("t1", "노트1", "내용1", List.of("spring", "springboot")));
        noteService.create(userId, new NoteCreateRequest("t1", "노트2", "내용2", List.of("spring", "java")));

        List<TagAutoCompleteResponse> result = tagService.autocomplete(userId, "spr");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).tag()).isEqualTo("spring");
        assertThat(result.get(0).count()).isEqualTo(2L);
    }

    @Test
    @DisplayName("여러 노트에 사용된 태그로 인기 태그 조회 시 빈도순으로 반환한다")
    void getPopular_multipleNotes_shouldReturnByFrequency() {
        Long userId = 1L;
        noteService.create(userId, new NoteCreateRequest("t1", "노트1", "내용1", List.of("java", "spring")));
        noteService.create(userId, new NoteCreateRequest("t1", "노트2", "내용2", List.of("java")));
        noteService.create(userId, new NoteCreateRequest("t1", "노트3", "내용3", List.of("java")));

        List<PopularTagResponse> result = tagService.getPopular(5);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).tag()).isEqualTo("java");
        assertThat(result.get(0).count()).isEqualTo(3L);
    }

    @Test
    @DisplayName("태그 필터로 노트 조회 시 해당 태그를 가진 노트만 반환한다")
    void findAllByTag_tagFilter_shouldReturnOnlyMatchingNotes() {
        Long userId = 1L;
        noteService.create(userId, new NoteCreateRequest("t1", "스프링 노트", "내용", List.of("spring", "java")));
        noteService.create(userId, new NoteCreateRequest("t1", "파이썬 노트", "내용", List.of("python")));
        noteService.create(userId, new NoteCreateRequest("t1", "자바 노트", "내용", List.of("java")));

        Page<NoteResponse> result = noteService.findAllByTag(userId, "java", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(n -> n.tags().contains("java"));
    }

    @Test
    @DisplayName("태그 필터 조회 시 다른 사용자의 노트는 포함되지 않는다")
    void findAllByTag_anotherUsersNotes_shouldNotBeIncluded() {
        noteService.create(1L, new NoteCreateRequest("t1", "내 노트", "내용", List.of("spring")));
        noteService.create(2L, new NoteCreateRequest("t1", "타인 노트", "내용", List.of("spring")));

        Page<NoteResponse> result = noteService.findAllByTag(1L, "spring", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
