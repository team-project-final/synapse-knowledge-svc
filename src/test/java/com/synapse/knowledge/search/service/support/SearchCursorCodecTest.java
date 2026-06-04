package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchCursorCodecTest {

    private final SearchCursorCodec searchCursorCodec = new SearchCursorCodec();

    @Test
    @DisplayName("유효한 점수와 노트 아이디로 인코딩-디코딩하면 원본이 복원된다")
    void encodeDecode_validScoreAndNoteId_shouldRoundTrip() {
        // Given
        double score = 12.5d;
        long noteId = 42L;

        // When
        String encoded = searchCursorCodec.encode(score, noteId);
        SearchCursorCodec.CursorPayload decoded = searchCursorCodec.decode(encoded);

        // Then
        assertThat(decoded.score()).isEqualTo(score);
        assertThat(decoded.noteId()).isEqualTo(noteId);
    }

    @Test
    @DisplayName("잘못된 커서를 디코딩하면 IllegalArgumentException을 던진다")
    void decode_invalidCursor_shouldThrowIllegalArgumentException() {
        // Given
        String invalidCursor = "not-a-valid-cursor";

        // When & Then
        assertThatThrownBy(() -> searchCursorCodec.decode(invalidCursor))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
