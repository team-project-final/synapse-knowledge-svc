package com.synapse.knowledge.search.application;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SearchCursorCodec {

    public String encode(double score, long noteId) {
        String payload = score + ":" + noteId;
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public CursorPayload decode(String cursor) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            return new CursorPayload(Double.parseDouble(parts[0]), Long.parseLong(parts[1]));
        } catch (Exception ex) {
            throw new IllegalArgumentException("유효하지 않은 검색 커서입니다", ex);
        }
    }

    public record CursorPayload(double score, long noteId) {
    }
}
