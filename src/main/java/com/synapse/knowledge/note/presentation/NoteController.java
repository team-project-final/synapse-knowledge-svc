package com.synapse.knowledge.note.presentation;

import com.synapse.knowledge.note.application.NoteService;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping
    public NoteResponse create(@Valid @RequestBody NoteCreateRequest request) {
        Long mockUserId = 1L;
        return noteService.create(mockUserId, request);
    }

    @GetMapping
    public Page<NoteResponse> list(Pageable pageable) {
        Long mockUserId = 1L;
        return noteService.findAll(mockUserId, pageable);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id) {
        Long mockUserId = 1L;
        return noteService.getById(mockUserId, id);
    }

    @PatchMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteCreateRequest request) {
        Long mockUserId = 1L;
        return noteService.update(mockUserId, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Long mockUserId = 1L;
        noteService.delete(mockUserId, id);
    }
}
