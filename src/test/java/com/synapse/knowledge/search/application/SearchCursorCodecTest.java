package com.synapse.knowledge.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchCursorCodecTest {

    private final SearchCursorCodec searchCursorCodec = new SearchCursorCodec();

    @Test
    @DisplayName("encodeDecode_유효한점수와노트아이디_shouldRoundTrip")
    void encodeDecode_유효한점수와노트아이디_shouldRoundTrip() {
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
    @DisplayName("decode_잘못된커서_shouldThrowIllegalArgumentException")
    void decode_잘못된커서_shouldThrowIllegalArgumentException() {
        // Given
        String invalidCursor = "not-a-valid-cursor";

        // When & Then
        assertThatThrownBy(() -> searchCursorCodec.decode(invalidCursor))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
