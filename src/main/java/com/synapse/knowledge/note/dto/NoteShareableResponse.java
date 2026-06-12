package com.synapse.knowledge.note.dto;

import com.synapse.knowledge.note.entity.Note;
import java.util.List;

/**
 * 노트 공유 가능 여부 확인 응답.
 * 공유 가능하면 커뮤니티 공유글 등록에 필요한 title/description/tags 를 함께 내려주고,
 * 불가하면 {@code shareable=false} 와 {@link ShareableReason} 사유만 내려준다.
 */
public record NoteShareableResponse(
    Long noteId,
    boolean shareable,
    String title,
    String description,
    List<String> tags,
    String reason
) {
    public static NoteShareableResponse ok(Note note, String description) {
        return new NoteShareableResponse(
            note.getId(),
            true,
            note.getTitle(),
            description,
            note.getTags(),
            null
        );
    }

    public static NoteShareableResponse denied(Long noteId, ShareableReason reason) {
        return new NoteShareableResponse(noteId, false, null, null, null, reason.name());
    }
}
