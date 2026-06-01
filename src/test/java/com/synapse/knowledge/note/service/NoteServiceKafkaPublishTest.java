package com.synapse.knowledge.note.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.synapse.knowledge.global.util.MarkdownSanitizer;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.entity.NoteIdentityMap;
import com.synapse.knowledge.note.kafka.producer.NoteCreatedPublishRequested;
import com.synapse.knowledge.note.kafka.producer.NoteUpdatedPublishRequested;
import com.synapse.knowledge.note.repository.NoteIdentityMapRepository;
import com.synapse.knowledge.note.repository.NoteLinkRepository;
import com.synapse.knowledge.note.repository.NoteRepository;
import com.synapse.knowledge.note.service.support.WikiLinkParser;
import com.synapse.knowledge.shared.NoteChunkingRequested;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoteServiceKafkaPublishTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteIdentityMapRepository noteIdentityMapRepository;

    @Mock
    private NoteLinkRepository noteLinkRepository;

    @Mock
    private WikiLinkParser linkParser;

    @Mock
    private MarkdownSanitizer sanitizer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NoteService noteService;

    @Test
    @DisplayName("create_노트저장성공시_shouldKafka발행요청이벤트를함께발행")
    void create_노트저장성공시_shouldKafka발행요청이벤트를함께발행() {
        NoteCreateRequest request = new NoteCreateRequest("tenant-1", "제목", "본문", List.of("kafka"));
        Note savedNote = Note.create("tenant-1", 10L, "제목", "본문", "본문", List.of("kafka"));
        NoteIdentityMap identityMap = NoteIdentityMap.create(1L);
        ReflectionTestUtils.setField(savedNote, "id", 1L);
        ReflectionTestUtils.setField(savedNote, "createdAt", LocalDateTime.of(2026, 6, 1, 9, 0));
        given(sanitizer.sanitize("본문")).willReturn("본문");
        given(noteRepository.save(any(Note.class))).willReturn(savedNote);
        given(noteIdentityMapRepository.findById(1L)).willReturn(Optional.empty());
        given(noteIdentityMapRepository.save(any(NoteIdentityMap.class))).willReturn(identityMap);
        given(linkParser.parse("본문")).willReturn(Set.of());

        noteService.create(10L, request);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(3)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).anySatisfy(event -> {
            assertThat(event).isInstanceOf(NoteCreatedPublishRequested.class);
            NoteCreatedPublishRequested publishRequested = (NoteCreatedPublishRequested) event;
            assertThat(publishRequested.externalNoteId()).isEqualTo(identityMap.getExternalNoteId());
            assertThat(publishRequested.tenantId()).isEqualTo("tenant-1");
            assertThat(publishRequested.title()).isEqualTo("제목");
        });
        assertThat(eventCaptor.getAllValues()).anyMatch(NoteChunkingRequested.class::isInstance);
        assertThat(eventCaptor.getAllValues()).anyMatch(NoteSearchSyncRequested.class::isInstance);
    }

    @Test
    @DisplayName("update_노트수정성공시_shouldKafka수정발행요청이벤트를함께발행")
    void update_노트수정성공시_shouldKafka수정발행요청이벤트를함께발행() {
        NoteCreateRequest request = new NoteCreateRequest("tenant-1", "수정 제목", "수정 본문", List.of("search"));
        Note note = Note.create("tenant-1", 10L, "기존 제목", "기존 본문", "기존 본문", List.of("legacy"));
        NoteIdentityMap identityMap = NoteIdentityMap.create(1L);
        ReflectionTestUtils.setField(note, "id", 1L);
        ReflectionTestUtils.setField(note, "updatedAt", LocalDateTime.of(2026, 6, 1, 10, 30));
        given(noteRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(note));
        given(noteIdentityMapRepository.findById(1L)).willReturn(Optional.of(identityMap));
        given(sanitizer.sanitize("수정 본문")).willReturn("수정 본문");
        given(linkParser.parse("수정 본문")).willReturn(Set.of());

        noteService.update(10L, 1L, request);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(3)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getAllValues()).anySatisfy(event -> {
            assertThat(event).isInstanceOf(NoteUpdatedPublishRequested.class);
            NoteUpdatedPublishRequested publishRequested = (NoteUpdatedPublishRequested) event;
            assertThat(publishRequested.externalNoteId()).isEqualTo(identityMap.getExternalNoteId());
            assertThat(publishRequested.tenantId()).isEqualTo("tenant-1");
            assertThat(publishRequested.title()).isEqualTo("수정 제목");
        });
        assertThat(eventCaptor.getAllValues()).anyMatch(NoteChunkingRequested.class::isInstance);
        assertThat(eventCaptor.getAllValues()).anyMatch(NoteSearchSyncRequested.class::isInstance);
    }
}
