package com.synapse.knowledge.search.event;

import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import java.util.List;
import java.util.UUID;

public record NoteSearchSyncKafkaEvent(
    String       specversion,
    String       id,
    String       source,
    String       type,
    long         time,
    String       tenantid,
    String       datacontenttype,
    Long         noteId,
    UUID         externalNoteId,
    Long         userId,
    String       title,
    String       contentPlain,
    List<String> tags,
    boolean      deleted
) {
    public static NoteSearchSyncKafkaEvent from(NoteSearchSyncRequested event) {
        return new NoteSearchSyncKafkaEvent(
            "1.0",
            UUID.randomUUID().toString(),
            "knowledge-svc",
            "com.synapse.event.knowledge.NoteSearchSyncRequested",
            event.requestedAt().toEpochMilli(),
            event.tenantId(),
            "application/json",
            event.noteId(),
            event.externalNoteId(),
            event.userId(),
            event.title(),
            event.contentPlain(),
            event.tags() == null ? List.of() : event.tags(),
            event.deleted()
        );
    }
}


