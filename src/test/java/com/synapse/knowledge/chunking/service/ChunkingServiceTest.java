package com.synapse.knowledge.chunking.service;

import com.synapse.knowledge.chunking.client.LearningAiEmbeddingClient;
import com.synapse.knowledge.chunking.config.ChunkingAiProperties;
import com.synapse.knowledge.chunking.dto.ChunkMapper;
import com.synapse.knowledge.chunking.entity.NoteChunk;
import com.synapse.knowledge.chunking.repository.NoteChunkEmbeddingJdbcRepository;
import com.synapse.knowledge.chunking.repository.NoteChunkRepository;
import com.synapse.knowledge.chunking.service.support.TokenCounter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChunkingServiceTest {

    @Mock
    private NoteChunkRepository noteChunkRepository;

    @Mock
    private NoteChunkEmbeddingJdbcRepository noteChunkEmbeddingJdbcRepository;

    @Mock
    private LearningAiEmbeddingClient learningAiEmbeddingClient;

    @Mock
    private ChunkingAiProperties chunkingAiProperties;

    @Mock
    private ChunkMapper chunkMapper;

    private final TokenCounter tokenCounter = new TokenCounter();
    private final TransactionOperations transactionOperations = new ImmediateTransactionOperations();

    @DisplayName("chunkNote_빈본문_should기존청크만삭제하고저장하지않음")
    @Test
    void chunkNote_빈본문_should기존청크만삭제하고저장하지않음() {
        // Given
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
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
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        ArgumentCaptor<NoteChunk> captor = ArgumentCaptor.forClass(NoteChunk.class);
        when(chunkingAiProperties.enabled()).thenReturn(false);

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
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        List<NoteChunk> savedChunks = new ArrayList<>();
        when(noteChunkRepository.save(any(NoteChunk.class))).thenAnswer(invocation -> {
            NoteChunk noteChunk = invocation.getArgument(0);
            savedChunks.add(noteChunk);
            return noteChunk;
        });
        when(chunkingAiProperties.enabled()).thenReturn(false);
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
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        List<NoteChunk> savedChunks = new ArrayList<>();
        when(noteChunkRepository.save(any(NoteChunk.class))).thenAnswer(invocation -> {
            NoteChunk noteChunk = invocation.getArgument(0);
            savedChunks.add(noteChunk);
            return noteChunk;
        });
        when(chunkingAiProperties.enabled()).thenReturn(false);
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
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        ArgumentCaptor<NoteChunk> captor = ArgumentCaptor.forClass(NoteChunk.class);
        String content = """
            청킹은 긴 노트를 나누는 작업입니다.

            한국어 문장도 공백 기준으로 토큰 수를 세어 저장할 수 있습니다.
            """;
        when(chunkingAiProperties.enabled()).thenReturn(false);

        // When
        chunkingService.chunkNote(1L, "tenant1", content);

        // Then
        verify(noteChunkRepository).save(captor.capture());
        assertThat(captor.getValue().getChunkText()).contains("청킹은", "한국어");
        assertThat(captor.getValue().getTokenCount()).isGreaterThan(0);
    }

    @DisplayName("chunkNote_임베딩활성화_shouldvector업데이트를수행함")
    @Test
    void chunkNote_임베딩활성화_shouldvector업데이트를수행함() {
        // Given
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        when(chunkingAiProperties.enabled()).thenReturn(true);
        when(chunkingAiProperties.expectedDimensions()).thenReturn(3);
        when(learningAiEmbeddingClient.createEmbeddings("subject-123", List.of("첫 문단입니다.\n\n둘째 문단입니다.")))
            .thenReturn(new LearningAiEmbeddingClient.EmbeddingBatchResponse(List.of(List.of(0.1f, 0.2f, 0.3f)), "test-model"));
        when(noteChunkRepository.save(any(NoteChunk.class))).thenAnswer(invocation -> {
            NoteChunk noteChunk = invocation.getArgument(0);
            ReflectionTestUtils.setField(noteChunk, "id", 1L);
            return noteChunk;
        });

        // When
        chunkingService.chunkNote(1L, "subject-123", "첫 문단입니다.\n\n둘째 문단입니다.");

        // Then
        verify(noteChunkEmbeddingJdbcRepository).updateEmbedding(1L, List.of(0.1f, 0.2f, 0.3f));
    }

    @DisplayName("chunkNote_임베딩개수불일치_should예외를던지고저장하지않음")
    @Test
    void chunkNote_임베딩개수불일치_should예외를던지고저장하지않음() {
        // Given
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        when(chunkingAiProperties.enabled()).thenReturn(true);
        when(learningAiEmbeddingClient.createEmbeddings("subject-123", List.of("첫 문단입니다.\n\n둘째 문단입니다.")))
            .thenReturn(new LearningAiEmbeddingClient.EmbeddingBatchResponse(List.of(), "test-model"));

        // When & Then
        assertThatThrownBy(() -> chunkingService.chunkNote(1L, "subject-123", "첫 문단입니다.\n\n둘째 문단입니다."))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("임베딩 개수 불일치");
        verify(noteChunkRepository, never()).save(any(NoteChunk.class));
    }

    @DisplayName("chunkNote_임베딩차원불일치_should예외를던지고저장하지않음")
    @Test
    void chunkNote_임베딩차원불일치_should예외를던지고저장하지않음() {
        // Given
        ChunkingService chunkingService = new ChunkingService(
            noteChunkRepository,
            noteChunkEmbeddingJdbcRepository,
            learningAiEmbeddingClient,
            chunkingAiProperties,
            tokenCounter,
            chunkMapper,
            transactionOperations
        );
        when(chunkingAiProperties.enabled()).thenReturn(true);
        when(chunkingAiProperties.expectedDimensions()).thenReturn(3);
        when(learningAiEmbeddingClient.createEmbeddings("subject-123", List.of("첫 문단입니다.\n\n둘째 문단입니다.")))
            .thenReturn(new LearningAiEmbeddingClient.EmbeddingBatchResponse(List.of(List.of(0.1f, 0.2f)), "test-model"));

        // When & Then
        assertThatThrownBy(() -> chunkingService.chunkNote(1L, "subject-123", "첫 문단입니다.\n\n둘째 문단입니다."))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("임베딩 차원 불일치");
        verify(noteChunkRepository, never()).save(any(NoteChunk.class));
    }

    private List<String> createTokens(String prefix, int count) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tokens.add(prefix + i);
        }
        return tokens;
    }

    private static final class ImmediateTransactionOperations implements TransactionOperations {
        @Override
        public <T> T execute(TransactionCallback<T> action) {
            TransactionStatus status = new SimpleTransactionStatus();
            return action.doInTransaction(status);
        }
    }
}
