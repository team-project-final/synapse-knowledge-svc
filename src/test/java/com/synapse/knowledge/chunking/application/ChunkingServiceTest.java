package com.synapse.knowledge.chunking.application;

import com.synapse.knowledge.chunking.dto.ChunkMapper;
import com.synapse.knowledge.chunking.domain.NoteChunk;
import com.synapse.knowledge.chunking.domain.NoteChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChunkingServiceTest {

    @Mock
    private NoteChunkRepository noteChunkRepository;

    @Mock
    private ChunkMapper chunkMapper;

    private final TokenCounter tokenCounter = new TokenCounter();

    @DisplayName("chunkNote_빈본문_should기존청크만삭제하고저장하지않음")
    @Test
    void chunkNote_빈본문_should기존청크만삭제하고저장하지않음() {
        // Given
        ChunkingService chunkingService = new ChunkingService(noteChunkRepository, tokenCounter, chunkMapper);

        // When
        chunkingService.chunkNote(1L, "tenant1", "   ");

        // Then
        verify(noteChunkRepository).deleteByNoteId(1L);
        verify(noteChunkRepository, never()).save(any(NoteChunk.class));
    }

    @DisplayName("chunkNote_짧은본문_should단일청크로저장")
    @Test
    void chunkNote_짧은본문_should단일청크로저장() {
        // Given
        ChunkingService chunkingService = new ChunkingService(noteChunkRepository, tokenCounter, chunkMapper);
        ArgumentCaptor<NoteChunk> captor = ArgumentCaptor.forClass(NoteChunk.class);

        // When
        chunkingService.chunkNote(1L, "tenant1", "첫 문단입니다.\n\n둘째 문단입니다.");

        // Then
        verify(noteChunkRepository).deleteByNoteId(1L);
        verify(noteChunkRepository).save(captor.capture());

        NoteChunk savedChunk = captor.getValue();
        assertThat(savedChunk.getNoteId()).isEqualTo(1L);
        assertThat(savedChunk.getChunkIndex()).isZero();
        assertThat(savedChunk.getChunkText()).contains("첫 문단입니다.", "둘째 문단입니다.");
        assertThat(savedChunk.getTokenCount()).isEqualTo(4);
    }

    @DisplayName("chunkNote_매우긴본문_should여러청크로저장")
    @Test
    void chunkNote_매우긴본문_should여러청크로저장() {
        // Given
        ChunkingService chunkingService = new ChunkingService(noteChunkRepository, tokenCounter, chunkMapper);
        List<NoteChunk> savedChunks = new ArrayList<>();
        when(noteChunkRepository.save(any(NoteChunk.class))).thenAnswer(invocation -> {
            NoteChunk noteChunk = invocation.getArgument(0);
            savedChunks.add(noteChunk);
            return noteChunk;
        });
        String longText = String.join(" ", createTokens("token", 600));

        // When
        chunkingService.chunkNote(1L, "tenant1", longText);

        // Then
        assertThat(savedChunks).hasSizeGreaterThan(1);
        assertThat(savedChunks.get(0).getChunkIndex()).isZero();
        assertThat(savedChunks.get(1).getChunkIndex()).isEqualTo(1);
        assertThat(savedChunks).allMatch(chunk -> chunk.getTokenCount() <= 512);
    }

    @DisplayName("chunkNote_긴본문_should다음청크가이전말미토큰을포함")
    @Test
    void chunkNote_긴본문_should다음청크가이전말미토큰을포함() {
        // Given
        ChunkingService chunkingService = new ChunkingService(noteChunkRepository, tokenCounter, chunkMapper);
        List<NoteChunk> savedChunks = new ArrayList<>();
        when(noteChunkRepository.save(any(NoteChunk.class))).thenAnswer(invocation -> {
            NoteChunk noteChunk = invocation.getArgument(0);
            savedChunks.add(noteChunk);
            return noteChunk;
        });
        String longText = String.join(" ", createTokens("overlap", 620));

        // When
        chunkingService.chunkNote(1L, "tenant1", longText);

        // Then
        assertThat(savedChunks).hasSizeGreaterThan(1);

        List<String> firstChunkTokens = tokenCounter.tokenize(savedChunks.get(0).getChunkText());
        List<String> secondChunkTokens = tokenCounter.tokenize(savedChunks.get(1).getChunkText());
        List<String> expectedOverlap = firstChunkTokens.subList(firstChunkTokens.size() - 50, firstChunkTokens.size());

        assertThat(secondChunkTokens.subList(0, 50)).containsExactlyElementsOf(expectedOverlap);
    }

    @DisplayName("chunkNote_한국어본문_should정상적으로청크를생성")
    @Test
    void chunkNote_한국어본문_should정상적으로청크를생성() {
        // Given
        ChunkingService chunkingService = new ChunkingService(noteChunkRepository, tokenCounter, chunkMapper);
        ArgumentCaptor<NoteChunk> captor = ArgumentCaptor.forClass(NoteChunk.class);
        String content = """
            청킹은 긴 노트를 나누는 작업입니다.

            한국어 문장도 공백 기준으로 토큰 수를 세어 저장할 수 있습니다.
            """;

        // When
        chunkingService.chunkNote(1L, "tenant1", content);

        // Then
        verify(noteChunkRepository).save(captor.capture());
        assertThat(captor.getValue().getChunkText()).contains("청킹은", "한국어");
        assertThat(captor.getValue().getTokenCount()).isGreaterThan(0);
    }

    private List<String> createTokens(String prefix, int count) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tokens.add(prefix + i);
        }
        return tokens;
    }
}
