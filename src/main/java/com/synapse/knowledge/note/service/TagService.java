package com.synapse.knowledge.note.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.dto.PopularTagResponse;
import com.synapse.knowledge.note.dto.TagAutoCompleteResponse;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private static final int AUTOCOMPLETE_LIMIT = 10;
    private static final int POPULAR_CACHE_SIZE = 50;
    private static final Duration POPULAR_CACHE_TTL = Duration.ofHours(1);
    private static final String POPULAR_CACHE_KEY = "tags:popular";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<TagAutoCompleteResponse> autocomplete(Long userId, String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        List<Object[]> rows = entityManager.createNativeQuery(
            "SELECT nt.tag, COUNT(*) AS cnt " +
            "FROM note_tags nt " +
            "JOIN notes n ON nt.note_id = n.id " +
            "WHERE n.user_id = :userId " +
            "  AND n.deleted_at IS NULL " +
            "  AND nt.tag LIKE :prefix " +
            "GROUP BY nt.tag " +
            "ORDER BY cnt DESC"
        )
            .setParameter("userId", userId)
            .setParameter("prefix", prefix + "%")
            .setMaxResults(AUTOCOMPLETE_LIMIT)
            .getResultList();

        return rows.stream()
            .map(row -> new TagAutoCompleteResponse((String) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    public List<PopularTagResponse> getPopular(int limit) {
        String cached = redisTemplate.opsForValue().get(POPULAR_CACHE_KEY);
        if (cached != null) {
            try {
                List<PopularTagResponse> all = objectMapper.readValue(cached, new TypeReference<>() {});
                return all.stream().limit(limit).toList();
            } catch (Exception e) {
                log.warn("인기 태그 캐시 역직렬화 실패, DB에서 재조회합니다: {}", e.getMessage());
            }
        }

        List<PopularTagResponse> result = queryPopularFromDb();
        cachePopular(result);
        return result.stream().limit(limit).toList();
    }

    @SuppressWarnings("unchecked")
    private List<PopularTagResponse> queryPopularFromDb() {
        List<Object[]> rows = entityManager.createNativeQuery(
            "SELECT tag, COUNT(*) AS cnt FROM note_tags GROUP BY tag ORDER BY cnt DESC"
        )
            .setMaxResults(POPULAR_CACHE_SIZE)
            .getResultList();

        return rows.stream()
            .map(row -> new PopularTagResponse((String) row[0], ((Number) row[1]).longValue()))
            .toList();
    }

    private void cachePopular(List<PopularTagResponse> result) {
        try {
            redisTemplate.opsForValue().set(
                POPULAR_CACHE_KEY,
                objectMapper.writeValueAsString(result),
                POPULAR_CACHE_TTL.toSeconds(),
                TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("인기 태그 캐시 저장 실패: {}", e.getMessage());
        }
    }
}
