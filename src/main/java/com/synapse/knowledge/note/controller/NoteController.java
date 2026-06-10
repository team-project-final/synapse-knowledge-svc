package com.synapse.knowledge.note.controller;

import com.synapse.knowledge.global.response.ApiResponse;
import com.synapse.knowledge.global.security.CurrentUser;
import com.synapse.knowledge.global.security.CurrentUserAuth;
import com.synapse.knowledge.note.dto.NoteCreateRequest;
import com.synapse.knowledge.note.dto.NoteResponse;
import com.synapse.knowledge.note.service.NoteService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @PostMapping
    public ApiResponse<NoteResponse> create(
        @CurrentUserAuth CurrentUser currentUser,
        @Valid @RequestBody NoteCreateRequest request
    ) {
        return ApiResponse.success(noteService.create(currentUser.userId(), currentUser.subject(), request));
    }

    @GetMapping
    public ApiResponse<Page<NoteResponse>> list(
        @CurrentUserAuth CurrentUser currentUser,
        @RequestParam(required = false) String tag,
        @ParameterObject
        Pageable pageable
    ) {
        if (tag != null && !tag.isBlank()) {
            return ApiResponse.success(noteService.findAllByTag(currentUser.userId(), tag, pageable));
        }
        return ApiResponse.success(noteService.findAll(currentUser.userId(), pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoteResponse> get(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        return ApiResponse.success(noteService.getById(currentUser.userId(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<NoteResponse> update(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long id,
        @Valid @RequestBody NoteCreateRequest request
    ) {
        return ApiResponse.success(noteService.update(currentUser.userId(), currentUser.subject(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@CurrentUserAuth CurrentUser currentUser, @PathVariable Long id) {
        noteService.delete(currentUser.userId(), id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/backlinks")
    public ApiResponse<List<NoteResponse>> getBacklinks(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long id
    ) {
        return ApiResponse.success(noteService.getBacklinks(currentUser.userId(), id));
    }

    @GetMapping("/{id}/outlinks")
    public ApiResponse<List<NoteResponse>> getOutlinks(
        @CurrentUserAuth CurrentUser currentUser,
        @PathVariable Long id
    ) {
        return ApiResponse.success(noteService.getOutlinks(currentUser.userId(), id));
    }
}
