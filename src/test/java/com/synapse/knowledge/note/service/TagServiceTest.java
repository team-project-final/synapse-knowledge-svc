package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.dto.PopularTagResponse;
import com.synapse.knowledge.note.dto.TagAutoCompleteResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("빈 prefix로 자동완성 요청 시 빈 목록을 반환한다")
    void autocomplete_emptyPrefix_shouldReturnEmptyList() {
        // When
        List<TagAutoCompleteResponse> result = tagService.autocomplete(1L, "");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null prefix로 자동완성 요청 시 빈 목록을 반환한다")
    void autocomplete_nullPrefix_shouldReturnEmptyList() {
        // When
        List<TagAutoCompleteResponse> result = tagService.autocomplete(1L, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("인기 태그 Redis 캐시 히트 시 DB를 조회하지 않고 캐시 결과를 반환한다")
    void getPopular_cacheHit_shouldReturnCachedResultWithoutDbQuery() throws Exception {
        // Given
        List<PopularTagResponse> cached = List.of(
            new PopularTagResponse("spring", 5L),
            new PopularTagResponse("java", 3L)
        );
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("tags:popular")).willReturn(objectMapper.writeValueAsString(cached));

        // When
        List<PopularTagResponse> result = tagService.getPopular(1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tag()).isEqualTo("spring");
    }

    @Test
    @DisplayName("인기 태그 캐시 미스 시 DB를 조회하고 결과를 캐시에 저장한다")
    void getPopular_cacheMiss_shouldQueryDbAndCacheResult() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get(anyString())).willReturn(null);

        // When
        List<PopularTagResponse> result = tagService.getPopular(10);

        // Then
        verify(valueOps).set(anyString(), anyString(), anyLong(), any());
        assertThat(result).isEmpty();
    }
}
