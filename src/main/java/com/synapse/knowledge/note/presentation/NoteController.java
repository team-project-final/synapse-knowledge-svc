package com.synapse.knowledge.note.presentation;

import com.synapse.knowledge.note.application.NoteService;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.shared.CurrentUser;
import com.synapse.knowledge.shared.CurrentUserAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping
    public NoteResponse create(@CurrentUserAuth CurrentUser currentUser, @Valid @RequestBody NoteCreateRequest request) {
        return noteService.create(currentUser.userId(), request);
    }

    @GetMapping
    public Page<NoteResponse> list(@CurrentUserAuth CurrentUser currentUser, Pageable pageable) {
        return noteService.findAll(currentUser.userId(), pageable);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        return noteService.getById(currentUser.userId(), id);
    }

    @PatchMapping("/{id}")
    public NoteResponse update(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long id,
        @Valid @RequestBody NoteCreateRequest request
    ) {
        return noteService.update(currentUser.userId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        noteService.delete(currentUser.userId(), id);
    }

    @GetMapping("/{id}/backlinks")
    public List<NoteResponse> getBacklinks(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        return noteService.getBacklinks(currentUser.userId(), id);
    }

    @GetMapping("/{id}/outlinks")
    public List<NoteResponse> getOutlinks(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        return noteService.getOutlinks(currentUser.userId(), id);
    }
}
