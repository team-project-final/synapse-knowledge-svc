package com.synapse.knowledge.note.exception;

import com.synapse.knowledge.global.exception.ErrorCode;
import com.synapse.knowledge.global.exception.NotFoundException;

public class NoteNotFoundException extends NotFoundException {
    public NoteNotFoundException(Long noteId) {
        super(ErrorCode.NOTE_NOT_FOUND, "노트를 찾을 수 없습니다. noteId=" + noteId);
    }
}
