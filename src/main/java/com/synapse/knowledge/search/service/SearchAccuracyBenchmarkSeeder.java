package com.synapse.knowledge.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import com.synapse.knowledge.shared.NoteCommandPort;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchAccuracyBenchmarkSeeder {

    private static final String BENCHMARK_NOTES_RESOURCE = "search/accuracy/search-benchmark-notes.json";

    private final NoteCommandPort noteCommandPort;
    private final NoteSearchRepository noteSearchRepository;
    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;

    public BenchmarkContext seed() {
        Map<String, SeededNote> notesByBenchmarkId = loadBenchmarkNotes().stream()
            .map(seed -> Map.entry(seed.benchmarkId(), upsert(seed)))
            .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);

        waitUntilIndexed(notesByBenchmarkId.values().stream().toList());

        return new BenchmarkContext(
            searchProperties.accuracy().benchmarkUserId(),
            searchProperties.accuracy().benchmarkTenantId(),
            searchProperties.accuracy().semanticActorId(),
            notesByBenchmarkId
        );
    }

    private SeededNote upsert(BenchmarkNoteSeed seed) {
        NoteCommandPort.NoteCommandResult result = noteCommandPort.upsert(
            new NoteCommandPort.NoteUpsertCommand(
                searchProperties.accuracy().benchmarkTenantId(),
                searchProperties.accuracy().benchmarkUserId(),
                seed.title(),
                seed.contentMd(),
                seed.tags()
            )
        );
        return new SeededNote(seed.benchmarkId(), result.noteId(), result.externalNoteId(), result.title());
    }

    private void waitUntilIndexed(List<SeededNote> notes) {
        Instant deadline = Instant.now().plus(searchProperties.accuracy().indexingWaitTimeout());

        while (Instant.now().isBefore(deadline)) {
            boolean indexed = notes.stream().allMatch(this::isIndexed);
            if (indexed) {
                return;
            }
            sleepBriefly();
        }

        throw new IllegalStateException("benchmark notes were not indexed within " + searchProperties.accuracy().indexingWaitTimeout());
    }

    private boolean isIndexed(SeededNote note) {
        List<SearchCandidate> results = noteSearchRepository.searchKeywordCandidates(
            searchProperties.accuracy().benchmarkUserId(),
            note.title(),
            5,
            null
        );
        return results.stream().anyMatch(candidate -> note.noteId().equals(candidate.noteId()));
    }

    private List<BenchmarkNoteSeed> loadBenchmarkNotes() {
        try (InputStream inputStream = new ClassPathResource(BENCHMARK_NOTES_RESOURCE).getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<BenchmarkNoteSeed>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load benchmark notes resource: " + BENCHMARK_NOTES_RESOURCE, ex);
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(200L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("benchmark indexing wait interrupted", ex);
        }
    }

    record BenchmarkNoteSeed(
        String benchmarkId,
        String title,
        String contentMd,
        List<String> tags
    ) {
    }

    public record SeededNote(
        String benchmarkId,
        Long noteId,
        UUID externalNoteId,
        String title
    ) {
    }

    public record BenchmarkContext(
        Long userId,
        String tenantId,
        String semanticActorId,
        Map<String, SeededNote> notesByBenchmarkId
    ) {
        public SearchIdentity toSearchIdentity() {
            return new SearchIdentity(userId, semanticActorId);
        }
    }
}
